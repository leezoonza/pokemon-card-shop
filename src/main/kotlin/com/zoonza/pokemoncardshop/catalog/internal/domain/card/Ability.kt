package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Ability(
    @Column(nullable = false)
    val type: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val effect: String
)
