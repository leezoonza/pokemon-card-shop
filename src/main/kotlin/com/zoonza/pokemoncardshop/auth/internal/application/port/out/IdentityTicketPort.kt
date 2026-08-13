package com.zoonza.pokemoncardshop.auth.internal.application.port.out

import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import java.time.Duration

interface IdentityTicketPort {
    fun issue(identity: VerifiedExternalIdentity, ttl: Duration): String

    fun consume(identityTicket: String): VerifiedExternalIdentity
}