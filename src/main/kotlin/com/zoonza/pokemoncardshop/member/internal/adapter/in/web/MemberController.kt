package com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.common.response.ApiResponse
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.MemberNicknameUpdateRequest
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.NicknameAvailabilityResponse
import com.zoonza.pokemoncardshop.member.internal.application.dto.MemberNicknameUpdateCommand
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberCommandUseCase
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberQueryUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberQueryUseCase: MemberQueryUseCase,
    private val memberCommandUseCase: MemberCommandUseCase
) {
    @GetMapping("/nickname")
    fun checkNicknameAvailability(
        @RequestParam nickname: String
    ): ApiResponse<NicknameAvailabilityResponse> {
        val available = memberQueryUseCase.isNicknameAvailable(Nickname(nickname))

        return ApiResponse.success(NicknameAvailabilityResponse(available))
    }

    @PutMapping("/me/nickname")
    fun updateNickname(
        @AuthenticationPrincipal memberId: Long,
        @RequestBody request: MemberNicknameUpdateRequest
    ): ApiResponse<Unit> {
        val command = MemberNicknameUpdateCommand(memberId, request.nickname)

        memberCommandUseCase.updateNickname(command)

        return ApiResponse.success()
    }
}
