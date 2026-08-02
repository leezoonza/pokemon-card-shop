package com.zoonza.pokemoncardshop.auth.internal.domain

interface ExternalIdentityRepository {
    fun existsByProviderAndSubject(provider: IdentityProvider, subject: String): Boolean

    fun save(externalIdentity: ExternalIdentity): ExternalIdentity
}