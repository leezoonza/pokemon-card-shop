package com.zoonza.pokemoncardshop.member.api

interface MemberLoginApi {
    fun recordLogin(command: MemberLoginCommand): MemberLoginResult?
}