package com.zoonza.pokemoncardshop.auth.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException

enum class ExternalAccountProvider(val value: String) {
    GOOGLE("google"),
    ;

    companion object {
        fun from(value: String): ExternalAccountProvider =
            entries.find { it.value == value }
                ?: throw DomainException(AuthErrorCode.UNSUPPORTED_IDENTITY_PROVIDER)
    }
}