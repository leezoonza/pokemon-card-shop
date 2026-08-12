package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.*

@Entity
class EnergyDetail private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, columnDefinition = "TEXT")
    val effect: String,

    @Column(nullable = false)
    val type: String
) {
    companion object {
        fun register(info: EnergyDetailRegisterInfo): EnergyDetail =
            EnergyDetail(effect = info.effect, type = info.type)
    }
}
