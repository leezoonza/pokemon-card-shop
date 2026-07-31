package com.zoonza.pokemoncardshop.member.internal.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Member private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Embedded
    var nickname: Nickname,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: MemberRole,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    var lastLoginAt: LocalDateTime
) {
    companion object {
        fun register(
            nickname: Nickname,
            role: MemberRole,
            createdAt: LocalDateTime
        ): Member =
            Member(
                nickname = nickname,
                role = role,
                createdAt = createdAt,
                lastLoginAt = createdAt
            )
    }
}