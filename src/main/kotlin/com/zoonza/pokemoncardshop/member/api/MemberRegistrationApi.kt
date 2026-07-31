package com.zoonza.pokemoncardshop.member.api

interface MemberRegistrationApi {
    fun register(command: MemberRegisterCommand): MemberRegisterResult
}
