package com.zoonza.pokemoncardshop.auth.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.MySqlTestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@Import(MySqlTestcontainersConfiguration::class)
@ActiveProfiles("test")
@DataJpaTest
class ExternalIdentityJpaRepositoryTests @Autowired constructor(
    private val repository: ExternalIdentityJpaRepository,
    private val entityManager: EntityManager,
) {

    @Test
    fun `외부 신원을 저장하고 모든 속성을 조회한다`() {
        val createdAt = Instant.parse("2026-08-02T03:00:00Z")
        val saved = repository.saveAndFlush(
            ExternalIdentity.register(
                provider = IdentityProvider.GOOGLE,
                subject = "google-subject",
                memberId = 42L,
                createdAt = createdAt,
            ),
        )
        val identityId = saved.id

        entityManager.clear()

        val found = repository.findById(identityId).orElseThrow()

        identityId shouldBeGreaterThan 0L
        found.provider shouldBe IdentityProvider.GOOGLE
        found.subject shouldBe "google-subject"
        found.memberId shouldBe 42L
        found.createdAt shouldBe createdAt
    }

    @Test
    fun `같은 제공자와 식별자의 외부 신원이 있을 때만 존재한다고 응답한다`() {
        repository.saveAndFlush(
            ExternalIdentity.register(
                provider = IdentityProvider.GOOGLE,
                subject = "google-subject",
                memberId = 42L,
                createdAt = Instant.parse("2026-08-02T03:00:00Z"),
            ),
        )

        repository.existsByProviderAndSubject(
            IdentityProvider.GOOGLE,
            "google-subject",
        ) shouldBe true
        repository.existsByProviderAndSubject(
            IdentityProvider.GOOGLE,
            "different-subject",
        ) shouldBe false
    }

    @Test
    fun `제공자와 식별자로 외부 신원을 조회한다`() {
        val saved = repository.saveAndFlush(
            ExternalIdentity.register(
                provider = IdentityProvider.GOOGLE,
                subject = "google-subject",
                memberId = 42L,
                createdAt = Instant.parse("2026-08-02T03:00:00Z"),
            ),
        )

        entityManager.clear()

        repository.findByProviderAndSubject(
            IdentityProvider.GOOGLE,
            "google-subject",
        )?.id shouldBe saved.id
        repository.findByProviderAndSubject(
            IdentityProvider.GOOGLE,
            "unknown-subject",
        ) shouldBe null
    }
}
