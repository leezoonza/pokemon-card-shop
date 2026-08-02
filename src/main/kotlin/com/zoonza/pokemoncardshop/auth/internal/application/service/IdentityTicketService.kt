package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.IssueIdentityTicketUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import org.springframework.stereotype.Service

@Service
class IdentityTicketService(
    private val identityTicketStore: IdentityTicketStore,
    private val externalIdentityRepository: ExternalIdentityRepository
) : IssueIdentityTicketUseCase {

    override fun issue(identity: VerifiedExternalIdentity): IdentityTicketResult {
        val purpose = determinePurpose(identity)

        val ticket = identityTicketStore.issue(identity, purpose.ttl)

        return IdentityTicketResult(purpose, ticket)
    }

    private fun determinePurpose(
        identity: VerifiedExternalIdentity
    ): IdentityTicketPurpose {
        val isRegistered = externalIdentityRepository.existsByProviderAndSubject(
            identity.provider,
            identity.identifier
        )

        return if (isRegistered) {
            IdentityTicketPurpose.LOGIN
        } else {
            IdentityTicketPurpose.SIGNUP
        }
    }
}