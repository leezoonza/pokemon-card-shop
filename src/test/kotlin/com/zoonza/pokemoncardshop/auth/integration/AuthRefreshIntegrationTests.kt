package com.zoonza.pokemoncardshop.auth.integration

import com.jayway.jsonpath.JsonPath
import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.RefreshTokenCookieManager
import com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence.ExternalIdentityJpaRepository
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence.MemberJpaRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
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
import java.time.Instant

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthRefreshIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val authTokenIssuer: AuthTokenIssuer,
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
    fun `리프레시 토큰을 한 번만 사용하고 인증 상태를 갱신한다`() {
        val member = memberRepository.saveAndFlush(
            Member.register(
                nickname = Nickname("피카츄"),
                role = MemberRole.MEMBER,
                createdAt = Instant.parse("2026-08-01T03:00:00Z"),
            ),
        )
        val originalTokens = authTokenIssuer.issue(member.id, member.role.value)
        refreshTokenStore.save(
            member.id,
            originalTokens.refreshToken.value,
            originalTokens.refreshToken.ttl,
        )

        val result = mockMvc.post("/api/auth/refresh") {
            with(csrf())
            cookie(
                MockCookie(
                    RefreshTokenCookieManager.COOKIE_NAME,
                    originalTokens.refreshToken.value,
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
        val refreshTokenCookie = result.response
            .getHeaders(HttpHeaders.SET_COOKIE)
            .single { it.startsWith("${RefreshTokenCookieManager.COOKIE_NAME}=") }
        val rotatedRefreshToken = refreshTokenCookie
            .substringAfter("${RefreshTokenCookieManager.COOKIE_NAME}=")
            .substringBefore(';')

        rotatedRefreshToken shouldNotBe originalTokens.refreshToken.value
        refreshTokenCookie.shouldContain("HttpOnly")
        refreshTokenCookie.shouldContain("Path=/api/auth")

        val jwt = jwtDecoder.decode(accessToken)

        jwt.subject shouldBe member.id.toString()
        jwt.getClaimAsString("role") shouldBe MemberRole.MEMBER.value
        refreshTokenStore.consume(rotatedRefreshToken) shouldBe member.id

        mockMvc.post("/api/auth/refresh") {
            with(csrf())
            cookie(
                MockCookie(
                    RefreshTokenCookieManager.COOKIE_NAME,
                    originalTokens.refreshToken.value,
                ),
            )
        }
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.data.code") { value("AUTH-003") }
            }
    }
}
