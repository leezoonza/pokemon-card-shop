package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.resource

import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CardNameTranslationPort
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.io.Reader

@Component
class CsvCardNameTranslationAdapter(
    @Value("classpath:catalog/card_name.csv") resource: Resource,
) : CardNameTranslationPort {

    private val koreanNamesByEnglishName: Map<String, String?> =
        resource.inputStream.bufferedReader(Charsets.UTF_8).use(::readTranslations)

    override fun translate(englishName: String): String? =
        koreanNamesByEnglishName[englishName]

    private fun readTranslations(reader: Reader): Map<String, String?> {
        val records = CSV_FORMAT.parse(reader).use { it.records }
        require(records.isNotEmpty()) { "카드 이름 CSV가 비어 있습니다." }

        require(records.first().columns(removeBom = true) == HEADER) {
            "카드 이름 CSV 헤더는 en,ko 형식이어야 합니다."
        }

        return buildMap {
            records.drop(1).forEach { record ->
                require(record.size() == HEADER.size) {
                    "카드 이름 CSV ${record.recordNumber}행은 두 개의 열이어야 합니다."
                }

                val englishName = record[ENGLISH_NAME_COLUMN]
                require(englishName.isNotBlank()) {
                    "카드 이름 CSV ${record.recordNumber}행의 영문 이름이 비어 있습니다."
                }
                val koreanName = record[KOREAN_NAME_COLUMN].takeIf(String::isNotBlank)

                if (containsKey(englishName)) {
                    require(get(englishName) == koreanName) {
                        "카드 이름 CSV의 중복 영문 이름에 서로 다른 번역이 있습니다: $englishName"
                    }
                } else {
                    put(englishName, koreanName)
                }
            }
        }
    }

    private fun CSVRecord.columns(removeBom: Boolean = false): List<String> =
        mapIndexed { index, value ->
            if (removeBom && index == 0) value.removePrefix("\uFEFF") else value
        }

    private companion object {
        const val ENGLISH_NAME_COLUMN = 0
        const val KOREAN_NAME_COLUMN = 1
        val HEADER = listOf("en", "ko")
        val CSV_FORMAT: CSVFormat = CSVFormat.DEFAULT.builder()
            .setIgnoreEmptyLines(false)
            .get()
    }
}
