package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CardDetailTests {

    @Test
    fun `포켓몬 상세 정보를 등록한다`() {
        val detail = PokemonDetail.register(
            PokemonDetailRegisterInfo(
                dexIds = setOf(25),
                hp = 60,
                types = setOf("Lightning"),
                stage = "Basic",
                retreat = 1,
            ),
        )

        detail.dexIds shouldBe setOf(25)
        detail.hp shouldBe 60
        detail.types shouldBe setOf("Lightning")
        detail.stage shouldBe "Basic"
        detail.retreat shouldBe 1
    }

    @Test
    fun `트레이너 상세 정보를 등록한다`() {
        val detail = TrainerDetail.register(
            TrainerDetailRegisterInfo(
                effect = "카드를 2장 뽑는다.",
                type = "Supporter",
            ),
        )

        detail.effect shouldBe "카드를 2장 뽑는다."
        detail.type shouldBe "Supporter"
    }

    @Test
    fun `에너지 상세 정보를 등록한다`() {
        val detail = EnergyDetail.register(
            EnergyDetailRegisterInfo(
                effect = "무색 에너지 2개분으로 작용한다.",
                type = "Special",
            ),
        )

        detail.effect shouldBe "무색 에너지 2개분으로 작용한다."
        detail.type shouldBe "Special"
    }
}
