package com.zoonza.pokemoncardshop.auth.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException

enum class IdentityProvider(val value: String) {
    GOOGLE("google"),
    ;

    companion object {
        fun from(value: String): IdentityProvider =
            entries.find { it.value == value }
                ?: throw DomainException(AuthErrorCode.UNSUPPORTED_IDENTITY_PROVIDER)
    }
}