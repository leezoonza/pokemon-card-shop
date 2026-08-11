package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.*

@Entity
class TrainerDetail private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, columnDefinition = "TEXT")
    val effect: String,

    @Column(nullable = false)
    val type: String
)
