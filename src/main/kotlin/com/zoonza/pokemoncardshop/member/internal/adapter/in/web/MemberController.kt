package com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.common.response.ApiResponse
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.ChangeNicknameRequest
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.NicknameAvailabilityResponse
import com.zoonza.pokemoncardshop.member.internal.application.dto.ChangeNicknameCommand
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.ChangeNicknameUseCase
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.CheckNicknameAvailabilityUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val checkNicknameAvailabilityUseCase: CheckNicknameAvailabilityUseCase,
    private val changeNicknameUseCase: ChangeNicknameUseCase
) {
    @GetMapping("/nickname")
    fun checkNicknameAvailability(
        @RequestParam nickname: String
    ): ApiResponse<NicknameAvailabilityResponse> {
        val available = checkNicknameAvailabilityUseCase.isAvailable(Nickname(nickname))

        return ApiResponse.success(NicknameAvailabilityResponse(available))
    }

    @PutMapping("/me/nickname")
    fun changeNickname(
        @AuthenticationPrincipal memberId: Long,
        @RequestBody request: ChangeNicknameRequest
    ): ApiResponse<Unit> {
        val command = ChangeNicknameCommand(memberId, request.nickname)

        changeNicknameUseCase.change(command)

        return ApiResponse.success()
    }
}
