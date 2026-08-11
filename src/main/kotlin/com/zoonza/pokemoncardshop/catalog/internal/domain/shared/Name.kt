package com.zoonza.pokemoncardshop.catalog.internal.domain.shared

import jakarta.persistence.Embeddable

@Embeddable
data class Name(
    val en: String,
    val ko: String?
)