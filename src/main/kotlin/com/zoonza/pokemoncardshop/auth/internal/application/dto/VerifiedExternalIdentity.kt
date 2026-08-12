package com.zoonza.pokemoncardshop.auth.internal.application.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider

data class VerifiedExternalIdentity(
    val provider: ExternalAccountProvider,
    @param:JsonAlias("identifier")
    val subject: String
)
