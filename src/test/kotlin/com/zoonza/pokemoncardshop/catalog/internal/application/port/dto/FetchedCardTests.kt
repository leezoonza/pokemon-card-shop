package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.catalog.test.fake.TEST_REGISTERED_AT
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceCardFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FetchedCardTests {

    @Test
    fun `조회한 포켓몬 카드를 등록 정보로 변환한다`() {
        val sourceCard = sourceCardFixture().copy(
            abilities = listOf(
                SourceAbility("Ability", "Static Shock", "상대 포켓몬을 마비시킨다."),
            ),
            dexIds = listOf(25),
            hp = 60,
            types = listOf("Lightning"),
            attacks = listOf(
                SourceAttack(
                    name = "Thunder Shock",
                    cost = listOf("Lightning"),
                    effect = null,
                    damage = "30",
                ),
            ),
            weaknesses = listOf(SourceWeakRes("Fighting", "x2")),
            resistances = listOf(SourceWeakRes("Metal", "-30")),
            retreat = 1,
        )

        val info = fetchedCard(sourceCard).toCardRegisterInfo(
            expansionId = 1L,
            registeredAt = TEST_REGISTERED_AT,
        )

        info.expansionId shouldBe 1L
        info.name shouldBe Name(en = "Pikachu", ko = "피카츄")
        info.category shouldBe CardCategory.POKEMON
        info.rarity shouldBe CardRarity.RARE
        info.abilities.single().name shouldBe "Static Shock"
        info.pokemonDetail?.dexIds shouldBe setOf(25)
        info.pokemonDetail?.hp shouldBe 60
        info.pokemonDetail?.attacks?.single()?.damage shouldBe "30"
        info.pokemonDetail?.weaknesses?.single()?.value shouldBe "x2"
        info.pokemonDetail?.resistances?.single()?.value shouldBe "-30"
        info.pokemonDetail?.retreat shouldBe 1
        info.trainerDetail shouldBe null
        info.energyDetail shouldBe null
    }

    @Test
    fun `조회한 트레이너 카드는 트레이너 상세 정보만 변환한다`() {
        val info = fetchedCard(
            sourceCardFixture().copy(
                category = "Trainer",
                effect = "카드를 2장 뽑는다.",
                trainerType = "Supporter",
                energyType = "Special",
            ),
            nameKo = "박사의 연구",
        ).toCardRegisterInfo(
            expansionId = 1L,
            registeredAt = TEST_REGISTERED_AT,
        )

        info.pokemonDetail shouldBe null
        info.trainerDetail?.effect shouldBe "카드를 2장 뽑는다."
        info.trainerDetail?.type shouldBe "Supporter"
        info.energyDetail shouldBe null
    }

    @Test
    fun `조회한 에너지 카드는 에너지 상세 정보만 변환한다`() {
        val info = fetchedCard(
            sourceCardFixture().copy(
                category = "Energy",
                effect = "무색 에너지 2개분으로 작용한다.",
                trainerType = "Item",
                energyType = "Special",
            ),
            nameKo = "더블 무색 에너지",
        ).toCardRegisterInfo(
            expansionId = 1L,
            registeredAt = TEST_REGISTERED_AT,
        )

        info.pokemonDetail shouldBe null
        info.trainerDetail shouldBe null
        info.energyDetail?.effect shouldBe "무색 에너지 2개분으로 작용한다."
        info.energyDetail?.type shouldBe "Special"
    }

    @Test
    fun `이미지가 없으면 카드 등록 정보로 변환하지 않는다`() {
        val exception = shouldThrow<DomainException> {
            fetchedCard(sourceCardFixture().copy(imageUrl = null)).toCardRegisterInfo(
                expansionId = 1L,
                registeredAt = TEST_REGISTERED_AT,
            )
        }

        exception.errorCode shouldBe CatalogImportErrorCode.CARD_IMAGE_REQUIRED
    }

    private fun fetchedCard(
        source: SourceCard,
        nameKo: String? = "피카츄",
    ): FetchedCard = FetchedCard(source, nameKo)
}
