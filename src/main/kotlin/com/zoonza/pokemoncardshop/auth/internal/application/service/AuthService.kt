package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LoginUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.SignupUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberLoginApi
import com.zoonza.pokemoncardshop.member.api.MemberLoginCommand
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
    private val memberLoginApi: MemberLoginApi,
    private val externalIdentityRepository: ExternalIdentityRepository
) : SignupUseCase,
    LoginUseCase {

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

    @Transactional
    override fun login(identityTicket: String): AuthTokens {
        val verifiedIdentity = identityTicketStore.consume(identityTicket)

        val loggedInAt = Instant.now(clock)

        val externalIdentity = externalIdentityRepository
            .findByProviderAndSubject(verifiedIdentity.provider, verifiedIdentity.identifier)
            ?: throw DomainException(AuthErrorCode.EXTERNAL_IDENTITY_NOT_FOUND)

        val result = memberLoginApi.recordLogin(
            MemberLoginCommand(
                memberId = externalIdentity.memberId,
                loggedInAt = loggedInAt,
            )
        )

        val authTokens = authTokenIssuer.issue(
            externalIdentity.memberId,
            result.role
        )

        refreshTokenStore.save(
            externalIdentity.memberId,
            authTokens.refreshToken.value,
            authTokens.refreshToken.ttl
        )

        return authTokens
    }
}
