package com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccount
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import org.springframework.stereotype.Repository

@Repository
class JpaExternalAccountRepositoryAdapter(
    private val repository: ExternalAccountJpaRepository
) : ExternalAccountRepository {

    override fun existsByProviderAndSubject(provider: ExternalAccountProvider, subject: String): Boolean {
        return repository.existsByProviderAndSubject(provider, subject)
    }

    override fun save(externalAccount: ExternalAccount): ExternalAccount {
        return repository.save(externalAccount)
    }

    override fun findByProviderAndSubject(provider: ExternalAccountProvider, subject: String): ExternalAccount? {
        return repository.findByProviderAndSubject(provider, subject)
    }
}