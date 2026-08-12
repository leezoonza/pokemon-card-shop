package com.zoonza.pokemoncardshop.auth.internal.domain

interface ExternalAccountRepository {
    fun existsByProviderAndSubject(provider: ExternalAccountProvider, subject: String): Boolean

    fun save(externalAccount: ExternalAccount): ExternalAccount

    fun findByProviderAndSubject(provider: ExternalAccountProvider, subject: String): ExternalAccount?
}