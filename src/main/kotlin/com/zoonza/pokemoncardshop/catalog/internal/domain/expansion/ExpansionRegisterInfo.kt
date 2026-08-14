package com.zoonza.pokemoncardshop.catalog.internal.domain.expansion

import java.time.Instant
import java.time.LocalDate

data class ExpansionRegisterInfo(
    val seriesId: Long,
    val sourceId: String,
    val name: String,
    val count: CardCount,
    val image: ExpansionImage,
    val releaseDate: LocalDate,
    val registeredAt: Instant,
)
