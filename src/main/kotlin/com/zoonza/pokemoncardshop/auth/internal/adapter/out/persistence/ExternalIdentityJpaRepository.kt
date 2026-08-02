package com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import org.springframework.data.jpa.repository.JpaRepository

interface ExternalIdentityJpaRepository : JpaRepository<ExternalIdentity, Long> {
    fun existsByProviderAndSubject(provider: IdentityProvider, subject: String): Boolean
}