package com.zoonza.pokemoncardshop.catalog.internal.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ExpansionImage(
    @Column(nullable = false)
    val logoUrl: String,

    @Column
    val symbolUrl: String?
)
