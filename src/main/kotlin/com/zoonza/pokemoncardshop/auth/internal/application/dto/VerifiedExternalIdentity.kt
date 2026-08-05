package com.zoonza.pokemoncardshop.auth.internal.application.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider

data class VerifiedExternalIdentity(
    val provider: IdentityProvider,
    @param:JsonAlias("identifier")
    val subject: String
)
