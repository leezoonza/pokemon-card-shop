package com.zoonza.pokemoncardshop.catalog.internal.domain.series

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import java.time.Instant
import java.time.LocalDate

data class SeriesRegisterInfo(
    val sourceId: String,
    val name: Name,
    val releaseDate: LocalDate,
    val registeredAt: Instant,
)
