package com.zoonza.pokemoncardshop.member.internal.domain

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
    val createdAt: Instant,

    @Column(nullable = false)
    var lastLoginAt: Instant,
) {
    fun recordLogin(loggedInAt: Instant) {
        lastLoginAt = loggedInAt
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
                createdAt = createdAt,
                lastLoginAt = createdAt
            )
    }
}