package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedIdentityTicket
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.IdentityTicketIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import org.springframework.stereotype.Service

@Service
class IdentityTicketService(
    private val identityTicketStore: IdentityTicketStore,
    private val externalAccountRepository: ExternalAccountRepository
) : IdentityTicketIssuer {

    override fun issue(identity: VerifiedExternalIdentity): IssuedIdentityTicket {
        val purpose = determinePurpose(identity)

        val ticket = identityTicketStore.issue(identity, purpose.ttl)

        return IssuedIdentityTicket(purpose, ticket)
    }

    private fun determinePurpose(
        identity: VerifiedExternalIdentity
    ): IdentityTicketPurpose {
        val isRegistered = externalAccountRepository.existsByProviderAndSubject(
            identity.provider,
            identity.subject
        )

        return if (isRegistered) {
            IdentityTicketPurpose.LOGIN
        } else {
            IdentityTicketPurpose.SIGNUP
        }
    }
}
