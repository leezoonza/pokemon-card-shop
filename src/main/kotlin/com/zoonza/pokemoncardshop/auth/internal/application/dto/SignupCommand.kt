package com.zoonza.pokemoncardshop.auth.internal.application.dto

import java.time.Instant

data class SignupCommand(
    val nickname: String,
    val identityTicket: String,
    val createdAt: Instant
)
