package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.Expansion
import org.springframework.data.jpa.repository.JpaRepository

interface ExpansionJpaRepository : JpaRepository<Expansion, Long> {
    fun existsBySourceId(sourceId: String): Boolean
}
