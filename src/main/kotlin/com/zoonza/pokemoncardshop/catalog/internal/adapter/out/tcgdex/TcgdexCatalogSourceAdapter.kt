package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.tcgdex

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.github.oshai.kotlinlogging.KotlinLogging
import net.tcgdex.sdk.Extension
import net.tcgdex.sdk.Quality
import net.tcgdex.sdk.TCGdex
import net.tcgdex.sdk.models.Card
import net.tcgdex.sdk.models.Serie
import net.tcgdex.sdk.models.Set
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

@Component
class TcgdexCatalogSourceAdapter(
    private val client: TCGdex,
) : CatalogSourceFetcher {

    override fun fetchSeriesSummaries(): List<SourceSeriesSummary> =
        fetchSource {
            client.fetchSeries()
                ?.map { series ->
                    SourceSeriesSummary(
                        sourceId = series.id,
                        name = series.name,
                        logoUrl = series.getLogoUrl(Extension.PNG),
                    )
                }
                ?: sourceNotFound()
        }

    override fun fetchSeries(sourceId: String): SourceSeries =
        fetchSource {
            client.fetchSerie(sourceId)
                ?.toSourceSeries()
                ?: sourceNotFound()
        }

    override fun getExpansion(sourceId: String): SourceExpansion =
        fetchSource {
            client.fetchSet(sourceId)
                ?.toSourceExpansion()
                ?: sourceNotFound()
        }

    override fun getCard(sourceId: String): SourceCard =
        fetchSource {
            client.fetchCard(sourceId)
                ?.toSourceCard()
                ?: sourceNotFound()
        }

    private fun Serie.toSourceSeries(): SourceSeries = SourceSeries(
        sourceId = id,
        name = name,
        logoUrl = getLogoUrl(Extension.PNG),
        expansions = sets.map { expansion ->
            SourceExpansionSummary(
                sourceId = expansion.id,
                name = expansion.name,
                logoUrl = expansion.getLogoUrl(Extension.PNG),
                symbolUrl = expansion.getSymbolUrl(Extension.PNG),
            )
        },
    )

    private fun Set.toSourceExpansion(): SourceExpansion = SourceExpansion(
        sourceId = id.requiredSourceValue(),
        seriesSourceId = serie.id,
        name = name.requiredSourceValue(),
        logoUrl = getLogoUrl(Extension.PNG),
        symbolUrl = getSymbolUrl(Extension.PNG),
        releaseDate = releaseDate.toSourceDate(),
        totalCardCount = cardCount.total,
        officialCardCount = cardCount.official,
        cardSourceIds = cards.map { it.id },
    )

    private fun Card.toSourceCard(): SourceCard {
        val sourceVariants = variants

        return SourceCard(
            sourceId = id,
            expansionSourceId = set.id,
            category = category,
            number = localId,
            name = name,
            imageUrl = image?.let { getImageUrl(Quality.HIGH, Extension.WEBP) },
            illustrator = illustrator,
            rarity = rarity,
            variants = SourceCardVariants(
                firstEdition = sourceVariants?.firstEdition ?: false,
                holo = sourceVariants?.holo ?: false,
                normal = sourceVariants?.normal ?: false,
                reverse = sourceVariants?.reverse ?: false,
                wPromo = sourceVariants?.wPromo ?: false,
            ),
        )
    }

    private fun String?.requiredSourceValue(): String =
        this?.takeIf(String::isNotBlank) ?: invalidSourceData()

    private fun String?.toSourceDate(): LocalDate {
        val value = requiredSourceValue()
        return try {
            LocalDate.parse(value)
        } catch (exception: DateTimeParseException) {
            throw DomainException(CatalogImportErrorCode.SOURCE_DATA_INVALID, exception)
        }
    }

    private fun <T> fetchSource(block: () -> T): T =
        try {
            block()
        } catch (exception: DomainException) {
            throw exception
        } catch (exception: FileNotFoundException) {
            throw DomainException(CatalogImportErrorCode.SOURCE_DATA_NOT_FOUND, exception)
        } catch (exception: IOException) {
            logger.warn(exception) { "TCGdex 카탈로그 조회에 실패했습니다." }
            throw DomainException(CatalogImportErrorCode.SOURCE_UNAVAILABLE, exception)
        } catch (exception: RuntimeException) {
            logger.warn(exception) { "TCGdex 카탈로그 데이터 변환에 실패했습니다." }
            throw DomainException(CatalogImportErrorCode.SOURCE_DATA_INVALID, exception)
        }

    private fun sourceNotFound(): Nothing =
        throw DomainException(CatalogImportErrorCode.SOURCE_DATA_NOT_FOUND)

    private fun invalidSourceData(): Nothing =
        throw DomainException(CatalogImportErrorCode.SOURCE_DATA_INVALID)
}
