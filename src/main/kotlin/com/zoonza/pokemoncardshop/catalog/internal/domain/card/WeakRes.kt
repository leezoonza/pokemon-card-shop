package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class WeakRes(
    @Column(nullable = false)
    val type: String,

    @Column
    val value: String?
)


