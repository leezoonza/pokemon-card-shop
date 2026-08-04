package com.zoonza.pokemoncardshop.member.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException
import jakarta.persistence.Embeddable
import java.util.regex.Pattern

@Embeddable
data class Nickname(val value: String) {
    init {
        if (value.isBlank()) {
            throw DomainException(MemberErrorCode.INVALID_NICKNAME)
        }

        if (value.length !in MIN_LENGTH..MAX_LENGTH ||
            !ALLOWED_CHARACTERS.matcher(value).matches()
        ) {
            throw DomainException(MemberErrorCode.INVALID_NICKNAME)
        }
    }

    companion object {
        private const val MIN_LENGTH: Int = 2
        private const val MAX_LENGTH: Int = 14
        private val ALLOWED_CHARACTERS: Pattern = Pattern.compile("^[가-힣a-zA-Z0-9]+$")
    }
}