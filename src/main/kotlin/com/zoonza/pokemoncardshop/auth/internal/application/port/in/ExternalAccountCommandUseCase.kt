package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupResult

interface ExternalAccountCommandUseCase {
    fun signup(command: SignupCommand): SignupResult
}