package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.LoginResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.RefreshResult
import java.time.Instant

interface AuthenticationUseCase {
    fun login(identityTicket: String, loggedInAt: Instant): LoginResult

    fun refresh(refreshToken: String?): RefreshResult

    fun logout(refreshToken: String?)
}