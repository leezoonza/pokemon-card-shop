package com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import org.springframework.stereotype.Repository

@Repository
class JpaExternalIdentityRepositoryAdapter(
    private val repository: ExternalIdentityJpaRepository
) : ExternalIdentityRepository {

    override fun existsByProviderAndSubject(provider: IdentityProvider, subject: String): Boolean {
        return repository.existsByProviderAndSubject(provider, subject)
    }

    override fun save(externalIdentity: ExternalIdentity): ExternalIdentity {
        return repository.save(externalIdentity)
    }
}