package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand

interface SignupUseCase {
    fun signup(command: SignupCommand): AuthTokens
}