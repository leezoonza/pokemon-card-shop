package com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.MySqlTestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider
import com.zoonza.pokemoncardshop.auth.test.fake.TEST_AUTHENTICATED_AT
import com.zoonza.pokemoncardshop.auth.test.fake.externalIdentityFixture
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(MySqlTestcontainersConfiguration::class)
@ActiveProfiles("test")
@DataJpaTest
class ExternalAccountJpaRepositoryTests @Autowired constructor(
    private val repository: ExternalAccountJpaRepository,
    private val entityManager: EntityManager,
) {

    @Test
    fun `연동 계정을 저장하고 모든 속성을 조회한다`() {
        val saved = repository.saveAndFlush(externalIdentityFixture())
        val identityId = saved.id

        entityManager.clear()

        val found = repository.findById(identityId).orElseThrow()

        identityId shouldBeGreaterThan 0L
        found.provider shouldBe ExternalAccountProvider.GOOGLE
        found.subject shouldBe "google-subject"
        found.memberId shouldBe 42L
        found.linkedAt shouldBe TEST_AUTHENTICATED_AT.minusSeconds(60)
    }

    @Test
    fun `같은 제공자와 식별자의 연동 계정이 있을 때만 존재한다고 응답한다`() {
        repository.saveAndFlush(externalIdentityFixture())

        repository.existsByProviderAndSubject(
            ExternalAccountProvider.GOOGLE,
            "google-subject",
        ) shouldBe true
        repository.existsByProviderAndSubject(
            ExternalAccountProvider.GOOGLE,
            "different-subject",
        ) shouldBe false
    }

    @Test
    fun `제공자와 식별자로 연동 계정을 조회한다`() {
        val saved = repository.saveAndFlush(externalIdentityFixture())

        entityManager.clear()

        repository.findByProviderAndSubject(
            ExternalAccountProvider.GOOGLE,
            "google-subject",
        )?.id shouldBe saved.id
        repository.findByProviderAndSubject(
            ExternalAccountProvider.GOOGLE,
            "unknown-subject",
        ) shouldBe null
    }
}
