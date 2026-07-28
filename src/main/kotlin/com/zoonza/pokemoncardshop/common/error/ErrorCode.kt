package com.zoonza.pokemoncardshop.common.error

interface ErrorCode {
    val code: String
    val message: String
    val status: Int
}
