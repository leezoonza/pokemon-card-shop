package com.zoonza.pokemoncardshop.catalog.internal.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CardVariants(
    @Column(nullable = false)
    val firstEdition: Boolean = false,

    @Column(nullable = false)
    val holo: Boolean = false,

    @Column(nullable = false)
    val normal: Boolean = false,

    @Column(nullable = false)
    val reverse: Boolean = false,

    @Column(nullable = false)
    val wPromo: Boolean = false,
)
