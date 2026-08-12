package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.member.api.MemberAuthenticationApi
import com.zoonza.pokemoncardshop.member.api.MemberLoginCommand
import com.zoonza.pokemoncardshop.member.api.MemberLoginResult
import com.zoonza.pokemoncardshop.member.api.MemberRoleResult
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberAuthenticationService(
    private val memberFinder: MemberFinder
) : MemberAuthenticationApi {

    @Transactional
    override fun login(command: MemberLoginCommand): MemberLoginResult {
        val member = memberFinder.findById(command.memberId)

        member.login(command.loggedInAt)

        return MemberLoginResult(member.role.value)
    }

    override fun getMemberRole(memberId: Long): MemberRoleResult {
        val member = memberFinder.findById(memberId)

        return MemberRoleResult(member.role.value)
    }
}
