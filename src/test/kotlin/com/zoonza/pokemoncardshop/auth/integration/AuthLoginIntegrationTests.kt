package com.zoonza.pokemoncardshop.auth.integration

import com.jayway.jsonpath.JsonPath
import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.IdentityTicketCookieManager
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.RefreshTokenCookieManager
import com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence.ExternalIdentityJpaRepository
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence.MemberJpaRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockCookie
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.Instant

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthLoginIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val identityTicketStore: IdentityTicketStore,
    private val refreshTokenStore: RefreshTokenStore,
    private val memberRepository: MemberJpaRepository,
    private val externalIdentityRepository: ExternalIdentityJpaRepository,
    private val jwtDecoder: JwtDecoder,
) {

    @BeforeEach
    fun clearDatabase() {
        externalIdentityRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun `가입한 연동 계정으로 로그인하고 인증 상태를 갱신한다`() {
        val registeredAt = Instant.parse("2026-08-01T03:00:00Z")
        val member = memberRepository.saveAndFlush(
            Member.register(
                nickname = Nickname("피카츄"),
                role = MemberRole.MEMBER,
                createdAt = registeredAt,
            ),
        )
        val verifiedIdentity = VerifiedExternalIdentity(
            provider = IdentityProvider.GOOGLE,
            identifier = "google-subject",
        )
        externalIdentityRepository.saveAndFlush(
            ExternalIdentity.register(
                provider = verifiedIdentity.provider,
                subject = verifiedIdentity.identifier,
                memberId = member.id,
                createdAt = registeredAt,
            ),
        )
        val identityTicket = identityTicketStore.issue(
            verifiedIdentity,
            Duration.ofMinutes(10),
        )

        val result = mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .cookie(
                    MockCookie(
                        IdentityTicketCookieManager.COOKIE_NAME,
                        identityTicket,
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andReturn()

        val accessToken = JsonPath.read<String>(
            result.response.contentAsString,
            "$.data.accessToken",
        )
        val setCookieHeaders = result.response.getHeaders(HttpHeaders.SET_COOKIE)
        val identityTicketCookie = setCookieHeaders.single {
            it.startsWith("${IdentityTicketCookieManager.COOKIE_NAME}=")
        }
        val refreshTokenCookie = setCookieHeaders.single {
            it.startsWith("${RefreshTokenCookieManager.COOKIE_NAME}=")
        }
        val refreshToken = refreshTokenCookie
            .substringAfter("${RefreshTokenCookieManager.COOKIE_NAME}=")
            .substringBefore(';')

        identityTicketCookie.shouldContain("Max-Age=0")
        refreshTokenCookie.shouldContain("HttpOnly")
        refreshTokenCookie.shouldContain("Path=/api/auth")

        val loggedInMember = memberRepository.findById(member.id).orElseThrow()

        loggedInMember.lastLoginAt shouldNotBe registeredAt

        val jwt = jwtDecoder.decode(accessToken)

        jwt.subject shouldBe member.id.toString()
        jwt.getClaimAsString("role") shouldBe MemberRole.MEMBER.value
        refreshTokenStore.consume(refreshToken) shouldBe member.id

        val exception = shouldThrow<DomainException> {
            identityTicketStore.consume(identityTicket)
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET
    }

    @Test
    fun `연동 계정에 연결된 회원이 없으면 회원 오류를 노출하지 않는다`() {
        val registeredAt = Instant.parse("2026-08-01T03:00:00Z")
        val verifiedIdentity = VerifiedExternalIdentity(
            provider = IdentityProvider.GOOGLE,
            identifier = "orphan-google-subject",
        )
        externalIdentityRepository.saveAndFlush(
            ExternalIdentity.register(
                provider = verifiedIdentity.provider,
                subject = verifiedIdentity.identifier,
                memberId = 999L,
                createdAt = registeredAt,
            ),
        )
        val identityTicket = identityTicketStore.issue(
            verifiedIdentity,
            Duration.ofMinutes(10),
        )

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .cookie(
                    MockCookie(
                        IdentityTicketCookieManager.COOKIE_NAME,
                        identityTicket,
                    ),
                ),
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.code").value("COMMON-001"))
            .andExpect(jsonPath("$.data.message").value("서버 내부 오류가 발생했습니다."))
    }
}
