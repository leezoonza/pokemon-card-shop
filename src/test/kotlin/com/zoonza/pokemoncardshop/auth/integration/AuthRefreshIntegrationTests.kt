package com.zoonza.pokemoncardshop.auth.integration

import com.jayway.jsonpath.JsonPath
import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.RefreshTokenCookieManager
import com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence.ExternalAccountJpaRepository
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence.MemberJpaRepository
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.test.fake.memberFixture
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

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthRefreshIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val authTokenIssuer: AuthTokenIssuer,
    private val refreshTokenStore: RefreshTokenStore,
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
    fun `리프레시 토큰을 한 번만 사용하고 인증 상태를 갱신한다`() {
        val member = memberRepository.saveAndFlush(memberFixture())
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

    @Test
    fun `리프레시 토큰의 연결 회원이 없으면 유효하지 않은 토큰으로 응답한다`() {
        val tokens = authTokenIssuer.issue(999L, MemberRole.MEMBER.value)
        refreshTokenStore.save(
            999L,
            tokens.refreshToken.value,
            tokens.refreshToken.ttl,
        )

        mockMvc.post("/api/auth/refresh") {
            with(csrf())
            cookie(
                MockCookie(
                    RefreshTokenCookieManager.COOKIE_NAME,
                    tokens.refreshToken.value,
                ),
            )
        }
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.data.code") { value(AuthErrorCode.INVALID_REFRESH_TOKEN.code) }
                jsonPath("$.data.message") {
                    value(AuthErrorCode.INVALID_REFRESH_TOKEN.message)
                }
            }
    }
}
