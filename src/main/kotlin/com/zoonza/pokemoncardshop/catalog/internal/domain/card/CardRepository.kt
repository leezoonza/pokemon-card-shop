package com.zoonza.pokemoncardshop.catalog.internal.domain.card

interface CardRepository {
    fun saveAll(cards: List<Card>): List<Card>
}
