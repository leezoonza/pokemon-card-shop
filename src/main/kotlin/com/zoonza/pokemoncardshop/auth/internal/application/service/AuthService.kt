package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LoginUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.ReissueAuthTokensUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.SignupUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.*
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
    private val memberRoleQueryApi: MemberRoleQueryApi,
    private val externalIdentityRepository: ExternalIdentityRepository
) : SignupUseCase,
    LoginUseCase,
    ReissueAuthTokensUseCase {

    @Transactional
    override fun signup(command: SignupCommand): AuthTokens {
        val verifiedIdentity = identityTicketStore.consume(command.identityTicket)

        val createdAt = Instant.now(clock)

        val memberRegisterCommand = MemberRegisterCommand(command.nickname, createdAt)

        val result = memberRegistrationApi.register(memberRegisterCommand)

        val externalIdentity = ExternalIdentity.register(
            provider = verifiedIdentity.provider,
            subject = verifiedIdentity.identifier,
            memberId = result.memberId,
            createdAt = createdAt
        )

        externalIdentityRepository.save(externalIdentity)

        val authTokens = authTokenIssuer.issue(result.memberId, result.role)

        refreshTokenStore.save(
            memberId = result.memberId,
            refreshToken = authTokens.refreshToken.value,
            ttl = authTokens.refreshToken.ttl
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

        val command = MemberLoginCommand(externalIdentity.memberId, loggedInAt)

        val result = memberLoginApi.recordLogin(command)
            ?: throw IllegalStateException("연동 계정에 연결된 회원 정보가 존재하지 않습니다.")


        val authTokens = authTokenIssuer.issue(
            externalIdentity.memberId,
            result.role
        )

        refreshTokenStore.save(
            memberId = externalIdentity.memberId,
            refreshToken = authTokens.refreshToken.value,
            ttl = authTokens.refreshToken.ttl
        )

        return authTokens
    }

    override fun reissue(refreshToken: String?): AuthTokens {
        val token = refreshToken
            ?.takeIf { it.isNotBlank() }
            ?: throw DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val memberId = refreshTokenStore.consume(token)

        val result = memberRoleQueryApi.findByMemberId(memberId)
            ?: throw DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val authTokens = authTokenIssuer.issue(memberId, result.role)

        refreshTokenStore.save(
            memberId = memberId,
            refreshToken = authTokens.refreshToken.value,
            ttl = authTokens.refreshToken.ttl,
        )

        return authTokens
    }
}
