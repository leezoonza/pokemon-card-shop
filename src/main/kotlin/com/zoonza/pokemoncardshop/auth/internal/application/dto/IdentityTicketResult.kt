package com.zoonza.pokemoncardshop.auth.internal.application.dto

import java.time.Duration

data class IdentityTicketResult(
    val purpose: IdentityTicketPurpose,
    val ticket: String
)

enum class IdentityTicketPurpose(val ttl: Duration) {
    SIGNUP(Duration.ofMinutes(10)),
    LOGIN(Duration.ofMinutes(10)),
}
