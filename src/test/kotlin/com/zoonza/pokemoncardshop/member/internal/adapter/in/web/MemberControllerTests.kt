package com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.global.exception.GlobalExceptionHandler
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.CheckNicknameAvailabilityUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class MemberControllerTests {

    private val checkNicknameAvailabilityUseCase = mockk<CheckNicknameAvailabilityUseCase>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(MemberController(checkNicknameAvailabilityUseCase))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `닉네임 사용 가능 여부를 응답한다`(available: Boolean) {
        val nickname = Nickname("피카츄")

        every { checkNicknameAvailabilityUseCase.isAvailable(nickname) } returns available

        mockMvc.perform(
            get("/api/members/nickname")
                .queryParam("nickname", nickname.value),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.available").value(available))

        verify(exactly = 1) { checkNicknameAvailabilityUseCase.isAvailable(nickname) }
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

        verify(exactly = 0) { checkNicknameAvailabilityUseCase.isAvailable(any()) }
    }
}
