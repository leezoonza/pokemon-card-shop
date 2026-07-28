package com.zoonza.pokemoncardshop.common.error

class DomainException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : RuntimeException(errorCode.message, cause)
