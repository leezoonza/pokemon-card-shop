package com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.common.response.ApiResponse
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.ChangeNicknameRequest
import com.zoonza.pokemoncardshop.member.internal.adapter.`in`.web.dto.NicknameAvailabilityResponse
import com.zoonza.pokemoncardshop.member.internal.application.dto.UpdateNicknameCommand
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberRegister
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberFinder: MemberFinder,
    private val memberRegister: MemberRegister
) {
    @GetMapping("/nickname")
    fun checkNicknameAvailability(
        @RequestParam nickname: String
    ): ApiResponse<NicknameAvailabilityResponse> {
        val available = memberFinder.isNicknameAvailable(Nickname(nickname))

        return ApiResponse.success(NicknameAvailabilityResponse(available))
    }

    @PutMapping("/me/nickname")
    fun changeNickname(
        @AuthenticationPrincipal memberId: Long,
        @RequestBody request: ChangeNicknameRequest
    ): ApiResponse<Unit> {
        val command = UpdateNicknameCommand(memberId, request.nickname)

        memberRegister.updateNickname(command)

        return ApiResponse.success()
    }
}
