package com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.global.exception.GlobalExceptionHandler
import com.zoonza.pokemoncardshop.member.internal.application.dto.UpdateNicknameCommand
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberRegister
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class MemberControllerTests {

    private val memberFinder = mockk<MemberFinder>()
    private val memberRegister = mockk<MemberRegister>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(MemberController(memberFinder, memberRegister))
        .setCustomArgumentResolvers(AuthenticationPrincipalArgumentResolver())
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @BeforeEach
    fun setAuthentication() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(42L, null, emptyList())
    }

    @AfterEach
    fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `닉네임 사용 가능 여부를 응답한다`(available: Boolean) {
        val nickname = Nickname("피카츄")

        every { memberFinder.isNicknameAvailable(nickname) } returns available

        mockMvc.perform(
            get("/api/members/nickname")
                .queryParam("nickname", nickname.value),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.available").value(available))

        verify(exactly = 1) { memberFinder.isNicknameAvailable(nickname) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["피", "피카츄!"])
    fun `올바르지 않은 닉네임은 오류 응답을 반환한다`(nickname: String) {
        mockMvc.perform(
            get("/api/members/nickname")
                .queryParam("nickname", nickname),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.code").value(MemberErrorCode.INVALID_NICKNAME.code))
            .andExpect(jsonPath("$.data.message").value(MemberErrorCode.INVALID_NICKNAME.message))
            .andExpect(jsonPath("$.data.errors").isEmpty())

        verify(exactly = 0) { memberFinder.isNicknameAvailable(any()) }
    }

    @Test
    fun `인증된 회원의 닉네임을 변경한다`() {
        val command = UpdateNicknameCommand(memberId = 42L, nickname = "라이츄")
        justRun { memberRegister.updateNickname(command) }

        mockMvc.perform(
            put("/api/members/me/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nickname":"라이츄"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify(exactly = 1) { memberRegister.updateNickname(command) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["피", "피카츄!"])
    fun `올바르지 않은 닉네임으로 변경할 수 없다`(nickname: String) {
        mockMvc.perform(
            put("/api/members/me/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nickname":"$nickname"}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.code").value(MemberErrorCode.INVALID_NICKNAME.code))
            .andExpect(jsonPath("$.data.message").value(MemberErrorCode.INVALID_NICKNAME.message))
            .andExpect(jsonPath("$.data.errors").isEmpty())

        verify(exactly = 0) { memberRegister.updateNickname(any()) }
    }
}
