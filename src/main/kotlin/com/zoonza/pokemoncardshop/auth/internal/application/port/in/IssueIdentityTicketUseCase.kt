package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity

interface IssueIdentityTicketUseCase {
    fun issue(identity: VerifiedExternalIdentity): IdentityTicketResult
}