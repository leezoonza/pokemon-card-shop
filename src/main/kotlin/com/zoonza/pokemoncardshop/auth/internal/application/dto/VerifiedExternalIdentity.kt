package com.zoonza.pokemoncardshop.auth.internal.application.dto

import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider

data class VerifiedExternalIdentity(
    val provider: IdentityProvider,
    val identifier: String
)
