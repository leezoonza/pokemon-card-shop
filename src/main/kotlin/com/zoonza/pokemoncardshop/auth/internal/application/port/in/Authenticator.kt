package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthenticationResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand

interface Authenticator {
    fun signup(command: SignupCommand): AuthenticationResult

    fun authenticate(identityTicket: String): AuthenticationResult

    fun reissue(refreshToken: String?): AuthenticationResult

    fun logout(refreshToken: String?)
}