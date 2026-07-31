package com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.common.response.ApiResponse
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.NicknameAvailabilityResponse
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.CheckNicknameAvailabilityUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val checkNicknameAvailabilityUseCase: CheckNicknameAvailabilityUseCase
) {
    @GetMapping("/nickname")
    fun checkNicknameAvailability(
        @RequestParam nickname: String
    ): ApiResponse<NicknameAvailabilityResponse> {
        val available = checkNicknameAvailabilityUseCase.check(Nickname(nickname))

        return ApiResponse.success(NicknameAvailabilityResponse(available))
    }
}