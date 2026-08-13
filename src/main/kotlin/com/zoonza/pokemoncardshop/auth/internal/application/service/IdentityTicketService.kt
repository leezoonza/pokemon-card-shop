package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedIdentityTicket
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.IdentityTicketUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import org.springframework.stereotype.Service

@Service
class IdentityTicketService(
    private val identityTicketPort: IdentityTicketPort,
    private val externalAccountRepository: ExternalAccountRepository
) : IdentityTicketUseCase {

    override fun issue(identity: VerifiedExternalIdentity): IssuedIdentityTicket {
        val purpose = determinePurpose(identity)

        val ticket = identityTicketPort.issue(identity, purpose.ttl)

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
