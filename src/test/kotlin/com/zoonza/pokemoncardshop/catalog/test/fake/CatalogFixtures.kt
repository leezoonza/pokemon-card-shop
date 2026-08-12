package com.zoonza.pokemoncardshop.catalog.test.fake

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.Expansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRegisterInfo
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import java.time.Instant
import java.time.LocalDate

val TEST_REGISTERED_AT: Instant = Instant.parse("2026-08-12T03:00:00Z")

fun seriesImportCommandFixture(
    seriesSourceId: String = "sv",
    seriesNameKo: String = "스칼렛&바이올렛",
    seriesReleaseDate: LocalDate = LocalDate.of(2023, 3, 31),
): SeriesImportCommand = SeriesImportCommand(
    seriesSourceId = seriesSourceId,
    seriesNameKo = seriesNameKo,
    seriesReleaseDate = seriesReleaseDate,
)

fun expansionImportSelectionCommandFixture(
    expansionSourceId: String = "sv01",
    expansionNameKo: String = "스칼렛&바이올렛",
): ExpansionImportSelectionCommand = ExpansionImportSelectionCommand(
    expansionSourceId = expansionSourceId,
    expansionNameKo = expansionNameKo,
)

fun expansionImportCommandFixture(
    seriesSourceId: String = "sv",
    expansions: List<ExpansionImportSelectionCommand> = listOf(
        expansionImportSelectionCommandFixture(),
    ),
): ExpansionImportCommand = ExpansionImportCommand(
    seriesSourceId = seriesSourceId,
    expansions = expansions,
)

fun sourceSeriesSummaryFixture(
    sourceId: String = "sv",
    name: String = "Scarlet & Violet",
    logoUrl: String? = "https://image/sv.png",
): SourceSeriesSummary = SourceSeriesSummary(
    sourceId = sourceId,
    name = name,
    logoUrl = logoUrl,
)

fun sourceExpansionSummaryFixture(
    sourceId: String = "sv01",
    name: String = "Scarlet & Violet",
    logoUrl: String? = "https://image/sv01.png",
    symbolUrl: String? = "https://image/sv01-symbol.png",
): SourceExpansionSummary = SourceExpansionSummary(
    sourceId = sourceId,
    name = name,
    logoUrl = logoUrl,
    symbolUrl = symbolUrl,
)

fun sourceSeriesFixture(
    sourceId: String = "sv",
    name: String = "Scarlet & Violet",
    logoUrl: String? = "https://image/sv.png",
    expansions: List<SourceExpansionSummary> = listOf(sourceExpansionSummaryFixture()),
): SourceSeries = SourceSeries(
    sourceId = sourceId,
    name = name,
    logoUrl = logoUrl,
    expansions = expansions,
)

fun sourceExpansionFixture(
    sourceId: String = "sv01",
    seriesSourceId: String = "sv",
    name: String = "Scarlet & Violet",
    logoUrl: String? = "https://image/sv01.png",
    symbolUrl: String? = "https://image/sv01-symbol.png",
    releaseDate: LocalDate = LocalDate.of(2023, 3, 31),
    totalCardCount: Int = 258,
    officialCardCount: Int = 198,
    cardSourceIds: List<String> = listOf("sv01-1"),
): SourceExpansion = SourceExpansion(
    sourceId = sourceId,
    seriesSourceId = seriesSourceId,
    name = name,
    logoUrl = logoUrl,
    symbolUrl = symbolUrl,
    releaseDate = releaseDate,
    totalCardCount = totalCardCount,
    officialCardCount = officialCardCount,
    cardSourceIds = cardSourceIds,
)

fun sourceCardFixture(): SourceCard = SourceCard(
    sourceId = "sv01-1",
    expansionSourceId = "sv01",
    category = "Pokemon",
    number = "1",
    name = "Pikachu",
    imageUrl = "https://image/sv01-1/high.webp",
    illustrator = null,
    rarity = "Rare",
    variants = SourceCardVariants(
        firstEdition = false,
        holo = true,
        normal = false,
        reverse = true,
        wPromo = false,
    ),
)

fun seriesFixture(
    sourceId: String = "sv",
    name: Name = Name(en = "Scarlet & Violet", ko = "스칼렛&바이올렛"),
    releaseDate: LocalDate = LocalDate.of(2023, 3, 31),
    registeredAt: Instant = TEST_REGISTERED_AT,
): Series = Series.register(
    sourceId = sourceId,
    name = name,
    releaseDate = releaseDate,
    registeredAt = registeredAt,
)

fun expansionFixture(
    seriesId: Long = 1L,
    sourceId: String = "sv01",
    name: Name = Name(en = "Scarlet & Violet", ko = "스칼렛&바이올렛"),
    count: CardCount = CardCount(total = 258, official = 198),
    image: ExpansionImage = ExpansionImage(
        logoUrl = "https://image/sv01.png",
        symbolUrl = "https://image/sv01-symbol.png",
    ),
    releaseDate: LocalDate = LocalDate.of(2023, 3, 31),
    registeredAt: Instant = TEST_REGISTERED_AT,
): Expansion = Expansion.register(
    ExpansionRegisterInfo(
        seriesId = seriesId,
        sourceId = sourceId,
        name = name,
        count = count,
        image = image,
        releaseDate = releaseDate,
        registeredAt = registeredAt,
    ),
)

fun cardRegisterInfoFixture(): CardRegisterInfo = CardRegisterInfo(
    expansionId = 1L,
    sourceId = "sv01-1",
    localId = "1",
    name = Name(en = "Pikachu", ko = "피카츄"),
    category = CardCategory.POKEMON,
    imageUrl = "https://image/sv01-1/high.webp",
    illustrator = null,
    rarity = CardRarity.RARE,
    variants = CardVariants(holo = true, reverse = true),
    registeredAt = TEST_REGISTERED_AT,
)

fun cardFixture(
    info: CardRegisterInfo = cardRegisterInfoFixture(),
): Card = Card.register(info)
