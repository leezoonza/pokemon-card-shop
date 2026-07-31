package com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.MySqlTestcontainersConfiguration
import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@Import(MySqlTestcontainersConfiguration::class)
@ActiveProfiles("test")
@DataJpaTest
class MemberJpaRepositoryTests @Autowired constructor(
    private val repository: MemberJpaRepository,
    private val entityManager: EntityManager,
) {

    @Test
    fun `회원을 저장하고 모든 속성을 조회한다`() {
        val nickname = Nickname("피카츄")
        val createdAt = LocalDateTime.of(2026, 7, 31, 12, 30)
        val saved = repository.saveAndFlush(
            Member.register(nickname, MemberRole.ADMIN, createdAt),
        )
        val memberId = saved.id

        entityManager.clear()

        val found = repository.findById(memberId).orElseThrow()

        memberId shouldBeGreaterThan 0L
        found.nickname shouldBe nickname
        found.role shouldBe MemberRole.ADMIN
        found.createdAt shouldBe createdAt
        found.lastLoginAt shouldBe createdAt
    }

    @Test
    fun `같은 닉네임의 회원이 있을 때만 존재한다고 응답한다`() {
        val nickname = Nickname("피카츄")

        repository.saveAndFlush(
            Member.register(
                nickname = nickname,
                role = MemberRole.MEMBER,
                createdAt = LocalDateTime.of(2026, 7, 31, 12, 30),
            ),
        )

        repository.existsByNickname(nickname) shouldBe true
        repository.existsByNickname(Nickname("라이츄")) shouldBe false
    }
}
