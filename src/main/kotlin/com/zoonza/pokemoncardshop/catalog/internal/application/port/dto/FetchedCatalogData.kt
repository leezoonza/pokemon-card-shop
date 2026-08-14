package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRegisterInfo
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardVariants
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRegisterInfo
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import java.time.Instant

data class FetchedCard(
    val source: SourceCard,
    val nameKo: String?,
) {
    fun toCardRegisterInfo(expansionId: Long, registeredAt: Instant): CardRegisterInfo {
        return CardRegisterInfo(
            expansionId = expansionId,
            sourceId = source.sourceId,
            localId = source.localId,
            name = Name(en = source.name, ko = nameKo),
            category = CardCategory.from(source.category),
            imageUrl = source.imageUrl,
            illustrator = source.illustrator,
            rarity = CardRarity.from(source.rarity),
            variants = source.variants.toCardVariants(),
            registeredAt = registeredAt,
        )
    }
}

data class FetchedExpansion(
    val source: SourceExpansion,
    val cards: List<FetchedCard>,
) {
    fun toExpansionRegisterInfo(seriesId: Long, registeredAt: Instant): ExpansionRegisterInfo =
        ExpansionRegisterInfo(
            seriesId = seriesId,
            sourceId = source.sourceId,
            name = source.name,
            count = CardCount(
                total = source.totalCardCount,
                official = source.officialCardCount,
            ),
            image = ExpansionImage(
                logoUrl = source.logoUrl,
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
