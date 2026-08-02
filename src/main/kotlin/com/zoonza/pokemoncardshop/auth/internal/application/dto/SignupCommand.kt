package com.zoonza.pokemoncardshop.auth.internal.application.dto

data class SignupCommand(
    val nickname: String,
    val identityTicket: String,
)
