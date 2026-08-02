package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.SignupUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.member.api.MemberRegisterCommand
import com.zoonza.pokemoncardshop.member.api.MemberRegistrationApi
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class AuthService(
    private val clock: Clock,
    private val authTokenIssuer: AuthTokenIssuer,
    private val refreshTokenStore: RefreshTokenStore,
    private val identityTicketStore: IdentityTicketStore,
    private val memberRegistrationApi: MemberRegistrationApi,
    private val externalIdentityRepository: ExternalIdentityRepository
) : SignupUseCase {

    @Transactional
    override fun signup(command: SignupCommand): AuthTokens {
        val verifiedIdentity = identityTicketStore.consume(command.identityTicket)

        val createdAt = Instant.now(clock)

        val result = memberRegistrationApi.register(
            MemberRegisterCommand(
                nickname = command.nickname,
                createdAt = createdAt,
            ),
        )

        val externalIdentity = ExternalIdentity.register(
            verifiedIdentity.provider,
            verifiedIdentity.identifier,
            result.memberId,
            createdAt
        )

        externalIdentityRepository.save(externalIdentity)

        val authTokens = authTokenIssuer.issue(
            result.memberId,
            result.role
        )

        refreshTokenStore.save(
            result.memberId,
            authTokens.refreshToken.value,
            authTokens.refreshToken.ttl
        )

        return authTokens
    }
}
