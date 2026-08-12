package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthenticationResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import java.time.Instant

interface Authenticator {
    fun signup(command: SignupCommand): AuthenticationResult

    fun login(identityTicket: String, loggedInAt: Instant): AuthenticationResult

    fun reissue(refreshToken: String?): AuthenticationResult

    fun logout(refreshToken: String?)
}