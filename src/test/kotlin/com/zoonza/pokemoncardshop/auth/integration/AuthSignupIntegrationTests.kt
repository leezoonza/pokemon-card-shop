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
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence.MemberJpaRepository
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockCookie
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Duration

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthSignupIntegrationTests @Autowired constructor(
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
    fun `검증된 연동 계정으로 가입하고 인증 상태를 생성한다`() {
        val verifiedIdentity = VerifiedExternalIdentity(
            provider = IdentityProvider.GOOGLE,
            subject = "google-subject",
        )
        val identityTicket = identityTicketStore.issue(
            verifiedIdentity,
            Duration.ofMinutes(10),
        )

        val result = mockMvc.post("/api/auth/signup") {
            with(csrf())
            cookie(
                MockCookie(
                    IdentityTicketCookieManager.COOKIE_NAME,
                    identityTicket,
                ),
            )
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"피카츄"}"""
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.accessToken") { isNotEmpty() }
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

        val members = memberRepository.findAll()

        members.shouldHaveSize(1)

        val member = members.single()

        member.nickname shouldBe Nickname("피카츄")
        member.role shouldBe MemberRole.MEMBER

        val identities = externalIdentityRepository.findAll()

        identities.shouldHaveSize(1)

        val externalIdentity = identities.single()

        externalIdentity.provider shouldBe verifiedIdentity.provider
        externalIdentity.subject shouldBe verifiedIdentity.subject
        externalIdentity.memberId shouldBe member.id

        val jwt = jwtDecoder.decode(accessToken)

        jwt.subject shouldBe member.id.toString()
        jwt.getClaimAsString("role") shouldBe MemberRole.MEMBER.value
        refreshTokenStore.consume(refreshToken) shouldBe member.id

        val exception = shouldThrow<DomainException> {
            identityTicketStore.consume(identityTicket)
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET
    }
}
