package com.zoonza.pokemoncardshop.catalog.internal.domain

interface CardRepository {
    fun saveAll(cards: List<Card>): List<Card>
}
