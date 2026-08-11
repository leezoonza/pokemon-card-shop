package com.zoonza.pokemoncardshop.catalog.internal.domain.expansion

interface ExpansionRepository {
    fun existsBySourceId(sourceId: String): Boolean

    fun save(expansion: Expansion): Expansion
}
