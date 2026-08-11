package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.*

/**
 * nullable:
 * - abilities
 * - attacks
 * - weaknesses
 * - resistances
 * - retreat
 */

@Entity
class PokemonDetail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ElementCollection
    @CollectionTable(
        name = "pokemon_dex_ids",
        joinColumns = [JoinColumn(name = "pokemon_detail_id")],
    )
    val dexIds: MutableSet<Int> = mutableSetOf(),

    @Column
    val hp: Int?,

    @ElementCollection
    @CollectionTable(
        name = "pokemon_types",
        joinColumns = [JoinColumn(name = "pokemon_detail_id")],
    )
    val types: MutableSet<String> = mutableSetOf(),

    @Column
    val evolveFrom: String?,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column
    val stage: String?,

    @Column
    val suffix: String?,

    @ElementCollection
    @CollectionTable(
        name = "pokemon_attacks",
        joinColumns = [JoinColumn(name = "pokemon_detail_id")],
    )
    val attacks: MutableList<Attack> = mutableListOf(),

    @ElementCollection
    @CollectionTable(
        name = "pokemon_weaknesses",
        joinColumns = [JoinColumn(name = "pokemon_detail_id")],
    )
    val weaknesses: MutableList<WeakRes> = mutableListOf(),

    @ElementCollection
    @CollectionTable(
        name = "pokemon_resistances",
        joinColumns = [JoinColumn(name = "pokemon_detail_id")],
    )
    val resistances: MutableList<WeakRes> = mutableListOf(),

    @Column
    val retreat: Int?
)
