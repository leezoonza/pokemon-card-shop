package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.resource

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource

class CsvCardNameTranslationAdapterTests {

    @Test
    fun `영문 이름이 정확히 일치하면 한글 이름을 찾는다`() {
        val adapter = adapter(
            """
            en,ko
            Absol ex,앱솔 ex
            Absol EX,앱솔 EX
            "Boss, Trainer","보스, 트레이너"
            """.trimIndent(),
        )

        adapter.translate("Absol ex") shouldBe "앱솔 ex"
        adapter.translate("Absol EX") shouldBe "앱솔 EX"
        adapter.translate("absol ex") shouldBe null
        adapter.translate("Boss, Trainer") shouldBe "보스, 트레이너"
    }

    @Test
    fun `한글 이름이 비었거나 영문 이름이 없으면 찾지 못한다`() {
        val adapter = adapter(
            """
            en,ko
            Pikachu,
            """.trimIndent(),
        )

        adapter.translate("Pikachu") shouldBe null
        adapter.translate("Raichu") shouldBe null
    }

    @Test
    fun `같은 번역의 중복 영문 이름은 하나로 합친다`() {
        val adapter = adapter(
            """
            en,ko
            Buizel,브이젤
            Buizel,브이젤
            """.trimIndent(),
        )

        adapter.translate("Buizel") shouldBe "브이젤"
    }

    @Test
    fun `배포 CSV의 알려진 중복 이름은 각각 한 번만 포함한다`() {
        val rows = ClassPathResource("catalog/card_name.csv")
            .inputStream
            .bufferedReader(Charsets.UTF_8)
            .use { it.readLines() }

        listOf("Buizel", "Combusken", "Glameow").forEach { englishName ->
            rows.count { it.startsWith("$englishName,") } shouldBe 1
        }
    }

    private fun adapter(csv: String): CsvCardNameTranslationAdapter =
        CsvCardNameTranslationAdapter(ByteArrayResource(csv.toByteArray(Charsets.UTF_8)))
}
