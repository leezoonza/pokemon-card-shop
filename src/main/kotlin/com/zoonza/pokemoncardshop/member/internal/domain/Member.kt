package com.zoonza.pokemoncardshop.member.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException
import jakarta.persistence.*
import java.time.Instant

@Entity
class Member private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Embedded
    @AttributeOverride(
        name = "value",
        column = Column(name = "nickname", unique = true, nullable = false)
    )
    var nickname: Nickname,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: MemberRole,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: MemberStatus,

    @Column(nullable = false)
    val createdAt: Instant,

    @Column(nullable = false)
    var lastLoginAt: Instant,
) {
    fun login(loggedInAt: Instant) {
        ensureActive()

        this.lastLoginAt = loggedInAt
    }

    fun updateNickname(nickname: Nickname) {
        ensureActive()

        this.nickname = nickname
    }

    private fun ensureActive() {
        if (status != MemberStatus.ACTIVE) {
            throw DomainException(MemberErrorCode.DEACTIVATED_MEMBER)
        }
    }

    companion object {
        fun register(
            nickname: Nickname,
            role: MemberRole,
            createdAt: Instant
        ): Member =
            Member(
                nickname = nickname,
                role = role,
                status = MemberStatus.ACTIVE,
                createdAt = createdAt,
                lastLoginAt = createdAt
            )
    }
}