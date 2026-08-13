package com.zoonza.pokemoncardshop.auth.integration

import com.jayway.jsonpath.JsonPath
import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.IdentityTicketCookieManager
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.RefreshTokenCookieManager
import com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence.ExternalAccountJpaRepository
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenPort
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.test.fake.externalIdentityFixture
import com.zoonza.pokemoncardshop.auth.test.fake.verifiedExternalIdentityFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence.MemberJpaRepository
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.test.fake.memberFixture
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
import org.springframework.test.web.servlet.post
import java.time.Duration
import java.time.Instant

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthLoginIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val identityTicketPort: IdentityTicketPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val memberRepository: MemberJpaRepository,
    private val externalIdentityRepository: ExternalAccountJpaRepository,
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
        val member = memberRepository.saveAndFlush(memberFixture(createdAt = registeredAt))
        val verifiedIdentity = verifiedExternalIdentityFixture()
        externalIdentityRepository.saveAndFlush(
            externalIdentityFixture(
                memberId = member.id,
                identity = verifiedIdentity,
                createdAt = registeredAt,
            ),
        )
        val identityTicket = identityTicketPort.issue(
            verifiedIdentity,
            Duration.ofMinutes(10),
        )

        val result = mockMvc.post("/api/auth/login") {
            with(csrf())
            cookie(
                MockCookie(
                    IdentityTicketCookieManager.COOKIE_NAME,
                    identityTicket,
                ),
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.accessToken") { isNotEmpty() }
                jsonPath("$.data.role") { value(MemberRole.MEMBER.value) }
            }
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
        refreshTokenPort.consume(refreshToken) shouldBe member.id

        val exception = shouldThrow<DomainException> {
            identityTicketPort.consume(identityTicket)
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET
    }

    @Test
    fun `연동 계정에 연결된 회원이 없으면 인증 실패를 응답한다`() {
        val registeredAt = Instant.parse("2026-08-01T03:00:00Z")
        val verifiedIdentity = verifiedExternalIdentityFixture().copy(
            subject = "orphan-google-subject",
        )
        externalIdentityRepository.saveAndFlush(
            externalIdentityFixture(
                memberId = 999L,
                identity = verifiedIdentity,
                createdAt = registeredAt,
            ),
        )
        val identityTicket = identityTicketPort.issue(
            verifiedIdentity,
            Duration.ofMinutes(10),
        )

        mockMvc.post("/api/auth/login") {
            with(csrf())
            cookie(
                MockCookie(
                    IdentityTicketCookieManager.COOKIE_NAME,
                    identityTicket,
                ),
            )
        }
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.data.code") { value(AuthErrorCode.AUTHENTICATION_FAILED.code) }
                jsonPath("$.data.message") {
                    value(AuthErrorCode.AUTHENTICATION_FAILED.message)
                }
            }
    }
}
