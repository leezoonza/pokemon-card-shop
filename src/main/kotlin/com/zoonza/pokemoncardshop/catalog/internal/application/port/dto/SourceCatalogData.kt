package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRegisterInfo
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.common.error.DomainException
import java.time.Instant
import java.time.LocalDate

data class SourceSeriesSummary(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
)

data class SourceSeries(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
    val expansions: List<SourceExpansionSummary>,
)

data class SourceExpansionSummary(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
    val symbolUrl: String?,
)

data class SourceExpansion(
    val sourceId: String,
    val seriesSourceId: String,
    val name: String,
    val logoUrl: String?,
    val symbolUrl: String?,
    val releaseDate: LocalDate,
    val totalCardCount: Int,
    val officialCardCount: Int,
    val cardSourceIds: List<String>,
) {
    fun toExpansionRegisterInfo(
        seriesId: Long,
        nameKo: String,
        registeredAt: Instant,
    ): ExpansionRegisterInfo = ExpansionRegisterInfo(
        seriesId = seriesId,
        sourceId = sourceId,
        name = Name(
            en = name,
            ko = nameKo.takeIf(String::isNotBlank)
                ?: throw DomainException(CatalogImportErrorCode.EXPANSION_KOREAN_NAME_REQUIRED),
        ),
        count = CardCount(
            total = totalCardCount,
            official = officialCardCount
        ),
        image = ExpansionImage(
            logoUrl = logoUrl?.takeIf(String::isNotBlank)
                ?: throw DomainException(CatalogImportErrorCode.EXPANSION_LOGO_REQUIRED),
            symbolUrl = symbolUrl
        ),
        releaseDate = releaseDate,
        registeredAt = registeredAt
    )
}

data class SourceCard(
    val sourceId: String,
    val expansionSourceId: String,
    val category: String,
    val number: String,
    val name: String,
    val imageUrl: String?,
    val illustrator: String?,
    val rarity: String,
    val variants: SourceCardVariants,
    val abilities: List<SourceAbility> = emptyList(),
    val dexIds: List<Int> = emptyList(),
    val hp: Int? = null,
    val types: List<String> = emptyList(),
    val evolveFrom: String? = null,
    val description: String? = null,
    val stage: String? = null,
    val suffix: String? = null,
    val attacks: List<SourceAttack> = emptyList(),
    val weaknesses: List<SourceWeakRes> = emptyList(),
    val resistances: List<SourceWeakRes> = emptyList(),
    val retreat: Int? = null,
    val effect: String? = null,
    val trainerType: String? = null,
    val energyType: String? = null,
) {
    fun toCardRegisterInfo(
        expansionId: Long,
        nameKo: String?,
        registeredAt: Instant,
    ): CardRegisterInfo {
        val cardCategory = CardCategory.from(category)

        return CardRegisterInfo(
            expansionId = expansionId,
            sourceId = sourceId,
            localId = number,
            name = Name(en = name, ko = nameKo),
            category = cardCategory,
            imageUrl = imageUrl
                ?: throw DomainException(CatalogImportErrorCode.CARD_IMAGE_REQUIRED),
            illustrator = illustrator,
            rarity = CardRarity.from(rarity),
            variants = CardVariants(
                firstEdition = variants.firstEdition,
                holo = variants.holo,
                normal = variants.normal,
                reverse = variants.reverse,
                wPromo = variants.wPromo,
            ),
            abilities = abilities.map {
                Ability(
                    type = it.type,
                    name = it.name,
                    effect = it.effect,
                )
            },
            pokemonDetail = takeIf { cardCategory == CardCategory.POKEMON }
                ?.toPokemonDetailRegisterInfo(),
            trainerDetail = takeIf { cardCategory == CardCategory.TRAINER }
                ?.toTrainerDetailRegisterInfo(),
            energyDetail = takeIf { cardCategory == CardCategory.ENERGY }
                ?.toEnergyDetailRegisterInfo(),
            registeredAt = registeredAt,
        )
    }

    fun toPokemonDetailRegisterInfo(): PokemonDetailRegisterInfo =
        PokemonDetailRegisterInfo(
            dexIds = dexIds.toSet(),
            hp = hp,
            types = types.toSet(),
            evolveFrom = evolveFrom,
            description = description,
            stage = stage,
            suffix = suffix,
            attacks = attacks.map {
                Attack(
                    name = it.name,
                    cost = it.cost,
                    effect = it.effect,
                    damage = it.damage,
                )
            },
            weaknesses = weaknesses.map {
                WeakRes(type = it.type, value = it.value)
            },
            resistances = resistances.map {
                WeakRes(type = it.type, value = it.value)
            },
            retreat = retreat,
        )

    fun toTrainerDetailRegisterInfo(): TrainerDetailRegisterInfo? {
        val sourceEffect = effect ?: return null
        val type = trainerType ?: return null

        return TrainerDetailRegisterInfo(sourceEffect, type)
    }

    fun toEnergyDetailRegisterInfo(): EnergyDetailRegisterInfo? {
        val sourceEffect = effect ?: return null
        val type = energyType ?: return null

        return EnergyDetailRegisterInfo(sourceEffect, type)
    }
}

data class SourceCardVariants(
    val firstEdition: Boolean,
    val holo: Boolean,
    val normal: Boolean,
    val reverse: Boolean,
    val wPromo: Boolean,
)

data class SourceAbility(
    val type: String,
    val name: String,
    val effect: String,
)

data class SourceAttack(
    val name: String,
    val cost: List<String>,
    val effect: String?,
    val damage: String?,
)

data class SourceWeakRes(
    val type: String,
    val value: String?,
)
