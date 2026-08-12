package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.catalog.test.fake.cardRegisterInfoFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CardTests {
    @Test
    fun `포켓몬 카드는 포켓몬 상세 정보만 연결한다`() {
        val card = Card.register(
            cardRegisterInfoFixture().copy(
                category = CardCategory.POKEMON,
                pokemonDetail = PokemonDetailRegisterInfo(
                    hp = 60,
                    stage = "Basic",
                    retreat = 1,
                ),
            ),
        )

        card.pokemonDetail?.hp shouldBe 60
        card.pokemonDetail?.stage shouldBe "Basic"
        card.pokemonDetail?.retreat shouldBe 1
        card.trainerDetail shouldBe null
        card.energyDetail shouldBe null
    }

    @Test
    fun `트레이너 카드는 포켓몬과 에너지 상세 정보를 연결하지 않는다`() {
        val card = Card.register(
            cardRegisterInfoFixture().copy(
                category = CardCategory.TRAINER,
                pokemonDetail = PokemonDetailRegisterInfo(hp = 60),
                trainerDetail = TrainerDetailRegisterInfo(
                    effect = "카드를 2장 뽑는다.",
                    type = "Supporter",
                ),
            ),
        )

        card.pokemonDetail shouldBe null
        card.trainerDetail?.effect shouldBe "카드를 2장 뽑는다."
        card.trainerDetail?.type shouldBe "Supporter"
        card.energyDetail shouldBe null
    }

    @Test
    fun `에너지 카드는 포켓몬과 트레이너 상세 정보를 연결하지 않는다`() {
        val card = Card.register(
            cardRegisterInfoFixture().copy(
                category = CardCategory.ENERGY,
                pokemonDetail = PokemonDetailRegisterInfo(hp = 60),
                trainerDetail = TrainerDetailRegisterInfo(
                    effect = "카드를 2장 뽑는다.",
                    type = "Supporter",
                ),
                energyDetail = EnergyDetailRegisterInfo(
                    effect = "무색 에너지 2개분으로 작용한다.",
                    type = "Special",
                ),
            ),
        )

        card.pokemonDetail shouldBe null
        card.trainerDetail shouldBe null
        card.energyDetail?.effect shouldBe "무색 에너지 2개분으로 작용한다."
        card.energyDetail?.type shouldBe "Special"
    }

    @Test
    fun `영문 이름이 비어 있으면 카드를 등록하지 않는다`() {
        val exception = shouldThrow<DomainException> {
            Card.register(
                cardRegisterInfoFixture().copy(
                    category = CardCategory.POKEMON,
                    name = Name(en = "", ko = null),
                ),
            )
        }

        exception.errorCode shouldBe CardErrorCode.ENGLISH_NAME_REQUIRED
    }
}
