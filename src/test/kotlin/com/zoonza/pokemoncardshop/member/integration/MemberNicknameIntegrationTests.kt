package com.zoonza.pokemoncardshop.member.integration

import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence.ExternalAccountJpaRepository
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenPort
import com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence.MemberJpaRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import com.zoonza.pokemoncardshop.member.test.fake.memberFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class MemberNicknameIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val authTokenPort: AuthTokenPort,
    private val memberRepository: MemberJpaRepository,
    private val externalIdentityRepository: ExternalAccountJpaRepository,
) {

    @BeforeEach
    fun clearDatabase() {
        externalIdentityRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun `인증된 회원의 닉네임을 변경하고 저장한다`() {
        val member = saveMember("피카츄")

        mockMvc.put("/api/members/me/nickname") {
            with(csrf())
            header(HttpHeaders.AUTHORIZATION, "Bearer ${accessTokenOf(member)}")
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"라이츄"}"""
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { doesNotExist() }
            }

        memberRepository.findById(member.id).orElseThrow().nickname shouldBe Nickname("라이츄")
        memberRepository.existsByNickname(Nickname("피카츄")) shouldBe false
    }

    @Test
    fun `이미 사용 중인 닉네임으로 변경하면 오류를 응답하고 기존 값을 유지한다`() {
        val member = saveMember("피카츄")
        saveMember("라이츄")

        mockMvc.put("/api/members/me/nickname") {
            with(csrf())
            header(HttpHeaders.AUTHORIZATION, "Bearer ${accessTokenOf(member)}")
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"라이츄"}"""
        }
            .andExpect {
                status { isConflict() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.data.code") { value(MemberErrorCode.DUPLICATE_NICKNAME.code) }
                jsonPath("$.data.message") { value(MemberErrorCode.DUPLICATE_NICKNAME.message) }
                jsonPath("$.data.errors") { isEmpty() }
            }

        memberRepository.findById(member.id).orElseThrow().nickname shouldBe Nickname("피카츄")
    }

    private fun saveMember(nickname: String): Member =
        memberRepository.saveAndFlush(
            memberFixture(nickname = Nickname(nickname)),
        )

    private fun accessTokenOf(member: Member): String =
        authTokenPort.issue(member.id, member.role.value).accessToken.value
}
