package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.*


@Entity
class PokemonDetail private constructor(
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
) {
    companion object {
        fun register(info: PokemonDetailRegisterInfo): PokemonDetail =
            PokemonDetail(
                dexIds = info.dexIds.toMutableSet(),
                hp = info.hp,
                types = info.types.toMutableSet(),
                evolveFrom = info.evolveFrom,
                description = info.description,
                stage = info.stage,
                suffix = info.suffix,
                attacks = info.attacks.toMutableList(),
                weaknesses = info.weaknesses.toMutableList(),
                resistances = info.resistances.toMutableList(),
                retreat = info.retreat,
            )
    }
}
