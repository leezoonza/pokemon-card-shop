package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRegisterInfo
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.common.error.DomainException
import java.time.Instant

data class FetchedCard(
    val source: SourceCard,
    val nameKo: String?,
) {
    fun toCardRegisterInfo(expansionId: Long, registeredAt: Instant): CardRegisterInfo {
        val category = CardCategory.from(source.category)

        return CardRegisterInfo(
            expansionId = expansionId,
            sourceId = source.sourceId,
            localId = source.number,
            name = Name(en = source.name, ko = nameKo),
            category = category,
            imageUrl = source.imageUrl
                ?: throw DomainException(CatalogImportErrorCode.CARD_IMAGE_REQUIRED),
            illustrator = source.illustrator,
            rarity = CardRarity.from(source.rarity),
            variants = source.variants.toCardVariants(),
            abilities = source.abilities.map { it.toAbility() },
            pokemonDetail = takeIf { category == CardCategory.POKEMON }
                ?.toPokemonDetailRegisterInfo(),
            trainerDetail = takeIf { category == CardCategory.TRAINER }
                ?.toTrainerDetailRegisterInfo(),
            energyDetail = takeIf { category == CardCategory.ENERGY }
                ?.toEnergyDetailRegisterInfo(),
            registeredAt = registeredAt,
        )
    }

    private fun toPokemonDetailRegisterInfo(): PokemonDetailRegisterInfo =
        PokemonDetailRegisterInfo(
            dexIds = source.dexIds.toSet(),
            hp = source.hp,
            types = source.types.toSet(),
            evolveFrom = source.evolveFrom,
            description = source.description,
            stage = source.stage,
            suffix = source.suffix,
            attacks = source.attacks.map { it.toAttack() },
            weaknesses = source.weaknesses.map { it.toWeakRes() },
            resistances = source.resistances.map { it.toWeakRes() },
            retreat = source.retreat,
        )

    private fun toTrainerDetailRegisterInfo(): TrainerDetailRegisterInfo? {
        val effect = source.effect ?: return null
        val type = source.trainerType ?: return null

        return TrainerDetailRegisterInfo(effect, type)
    }

    private fun toEnergyDetailRegisterInfo(): EnergyDetailRegisterInfo? {
        val effect = source.effect ?: return null
        val type = source.energyType ?: return null

        return EnergyDetailRegisterInfo(effect, type)
    }
}

data class FetchedExpansion(
    val source: SourceExpansion,
    val nameKo: String,
    val cards: List<FetchedCard>,
) {
    fun toExpansionRegisterInfo(seriesId: Long, registeredAt: Instant): ExpansionRegisterInfo =
        ExpansionRegisterInfo(
            seriesId = seriesId,
            sourceId = source.sourceId,
            name = Name(
                en = source.name,
                ko = nameKo.takeIf(String::isNotBlank)
                    ?: throw DomainException(CatalogImportErrorCode.EXPANSION_KOREAN_NAME_REQUIRED),
            ),
            count = CardCount(
                total = source.totalCardCount,
                official = source.officialCardCount,
            ),
            image = ExpansionImage(
                logoUrl = source.logoUrl?.takeIf(String::isNotBlank)
                    ?: throw DomainException(CatalogImportErrorCode.EXPANSION_LOGO_REQUIRED),
                symbolUrl = source.symbolUrl,
            ),
            releaseDate = source.releaseDate,
            registeredAt = registeredAt,
        )
}

private fun SourceCardVariants.toCardVariants(): CardVariants = CardVariants(
    firstEdition = firstEdition,
    holo = holo,
    normal = normal,
    reverse = reverse,
    wPromo = wPromo,
)

private fun SourceAbility.toAbility(): Ability = Ability(
    type = type,
    name = name,
    effect = effect,
)

private fun SourceAttack.toAttack(): Attack = Attack(
    name = name,
    cost = cost,
    effect = effect,
    damage = damage,
)

private fun SourceWeakRes.toWeakRes(): WeakRes = WeakRes(
    type = type,
    value = value,
)
