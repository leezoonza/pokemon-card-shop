package com.zoonza.pokemoncardshop.catalog.internal.domain

interface ExpansionRepository {
    fun existsBySourceId(sourceId: String): Boolean

    fun save(expansion: Expansion): Expansion
}
