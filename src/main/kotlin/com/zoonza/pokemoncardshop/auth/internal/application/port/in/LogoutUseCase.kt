package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

interface LogoutUseCase {
    fun logout(refreshToken: String?)
}
