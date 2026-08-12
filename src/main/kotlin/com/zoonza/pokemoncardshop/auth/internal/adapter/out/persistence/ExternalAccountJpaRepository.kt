package com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccount
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider
import org.springframework.data.jpa.repository.JpaRepository

interface ExternalAccountJpaRepository : JpaRepository<ExternalAccount, Long> {
    fun existsByProviderAndSubject(provider: ExternalAccountProvider, subject: String): Boolean

    fun findByProviderAndSubject(provider: ExternalAccountProvider, subject: String): ExternalAccount?
}