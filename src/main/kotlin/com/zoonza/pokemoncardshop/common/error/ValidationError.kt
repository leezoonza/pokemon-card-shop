package com.zoonza.pokemoncardshop.common.error

data class ValidationError(
    val field: String?,
    val message: String?,
)
