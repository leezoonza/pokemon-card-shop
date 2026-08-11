package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.ImportCatalogUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.RegisterCatalogData
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CatalogImportService(
    private val catalogSourceFetcher: CatalogSourceFetcher,
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
    private val registerCatalogData: RegisterCatalogData,
) : ImportCatalogUseCase {

    override fun importCatalog(command: CatalogImportCommand): CatalogImportResult {
        validateSelection(command.expansionSourceIds)

        val sourceSeries = catalogSourceFetcher.fetchSeries(command.seriesSourceId)
        requireLogo(sourceSeries.logoUrl, CatalogImportErrorCode.SERIES_LOGO_REQUIRED)
        validateExistingSeries(sourceSeries.sourceId, command.seriesReleaseDate)
        validateExpansionMembership(sourceSeries, command.expansionSourceIds)
        validateUnregisteredExpansions(command.expansionSourceIds)

        val expansions = command.expansionSourceIds.map { sourceId ->
            prepareExpansion(sourceSeries.sourceId, sourceId)
        }

        return registerCatalogData.register(
            CatalogRegistrationPlan(
                series = SeriesRegistrationData(
                    sourceId = sourceSeries.sourceId,
                    name = sourceSeries.name,
                    releaseDate = command.seriesReleaseDate,
                ),
                expansions = expansions,
            ),
        )
    }

    private fun validateSelection(expansionSourceIds: List<String>) {
        if (expansionSourceIds.isEmpty()) {
            throw DomainException(CatalogImportErrorCode.EMPTY_EXPANSION_SELECTION)
        }
        if (expansionSourceIds.size != expansionSourceIds.distinct().size) {
            throw DomainException(CatalogImportErrorCode.DUPLICATE_EXPANSION_SELECTION)
        }
    }

    private fun validateExistingSeries(
        sourceId: String,
        releaseDate: LocalDate,
    ) {
        val existingSeries = seriesRepository.findBySourceId(sourceId) ?: return
        if (existingSeries.releaseDate != releaseDate) {
            throw DomainException(CatalogImportErrorCode.SERIES_RELEASE_DATE_MISMATCH)
        }
    }

    private fun validateExpansionMembership(
        series: SourceSeries,
        selectedSourceIds: List<String>,
    ) {
        val expansionSourceIds = series.expansions.mapTo(mutableSetOf()) { it.sourceId }
        if (!expansionSourceIds.containsAll(selectedSourceIds)) {
            throw DomainException(CatalogImportErrorCode.EXPANSION_NOT_IN_SERIES)
        }
    }

    private fun validateUnregisteredExpansions(sourceIds: List<String>) {
        if (sourceIds.any(expansionRepository::existsBySourceId)) {
            throw DomainException(CatalogImportErrorCode.EXPANSION_ALREADY_REGISTERED)
        }
    }

    private fun prepareExpansion(
        seriesSourceId: String,
        expansionSourceId: String,
    ): ExpansionRegistrationData {
        val sourceExpansion = catalogSourceFetcher.getExpansion(expansionSourceId)
        if (sourceExpansion.seriesSourceId != seriesSourceId) {
            throw DomainException(CatalogImportErrorCode.EXPANSION_NOT_IN_SERIES)
        }
        val logoUrl = requireLogo(
            sourceExpansion.logoUrl,
            CatalogImportErrorCode.EXPANSION_LOGO_REQUIRED,
        )
        val cards = sourceExpansion.cardSourceIds.map { sourceId ->
            prepareCard(expansionSourceId, catalogSourceFetcher.getCard(sourceId))
        }

        return ExpansionRegistrationData(
            sourceId = sourceExpansion.sourceId,
            name = sourceExpansion.name,
            totalCardCount = sourceExpansion.totalCardCount,
            officialCardCount = sourceExpansion.officialCardCount,
            logoUrl = logoUrl,
            symbolUrl = sourceExpansion.symbolUrl?.takeIf(String::isNotBlank),
            releaseDate = sourceExpansion.releaseDate,
            cards = cards,
        )
    }

    private fun prepareCard(
        expansionSourceId: String,
        sourceCard: SourceCard,
    ): CardRegistrationData {
        if (sourceCard.expansionSourceId != expansionSourceId) {
            throw DomainException(CatalogImportErrorCode.CARD_NOT_IN_EXPANSION)
        }
        val imageUrl = sourceCard.imageUrl?.takeIf(String::isNotBlank)
            ?: throw DomainException(CatalogImportErrorCode.CARD_IMAGE_REQUIRED)

        return CardRegistrationData(
            sourceId = sourceCard.sourceId,
            category = sourceCard.category.toCardCategory(),
            number = sourceCard.number,
            name = sourceCard.name,
            imageUrl = imageUrl,
            illustrator = sourceCard.illustrator?.takeIf(String::isNotBlank) ?: "Unknown",
            rarity = sourceCard.rarity.toCardRarity(),
            variants = sourceCard.variants,
        )
    }

    private fun String.toCardCategory(): CardCategory =
        CardCategory.entries.firstOrNull { it.value.equals(this, ignoreCase = true) }
            ?: throw DomainException(CatalogImportErrorCode.UNSUPPORTED_CARD_CATEGORY)

    private fun String.toCardRarity(): CardRarity =
        CardRarity.entries.firstOrNull { it.value.equals(this, ignoreCase = true) }
            ?: throw DomainException(CatalogImportErrorCode.UNSUPPORTED_CARD_RARITY)

    private fun requireLogo(
        logoUrl: String?,
        errorCode: CatalogImportErrorCode,
    ): String = logoUrl?.takeIf(String::isNotBlank)
        ?: throw DomainException(errorCode)
}
