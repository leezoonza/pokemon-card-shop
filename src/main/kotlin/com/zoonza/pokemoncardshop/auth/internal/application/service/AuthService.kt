package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LoginUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LogoutUseCase
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
    LogoutUseCase,
    ReissueAuthTokensUseCase {

    @Transactional
    override fun signup(command: SignupCommand): IssuedAuthTokens {
        val verifiedIdentity = identityTicketStore.consume(command.identityTicket)

        val createdAt = Instant.now(clock)

        val registerMemberCommand = RegisterMemberCommand(command.nickname, createdAt)

        val result = memberRegistrationApi.register(registerMemberCommand)

        val externalIdentity = ExternalIdentity.register(
            provider = verifiedIdentity.provider,
            subject = verifiedIdentity.subject,
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
    override fun login(identityTicket: String): IssuedAuthTokens {
        val verifiedIdentity = identityTicketStore.consume(identityTicket)

        val loggedInAt = Instant.now(clock)

        val externalIdentity = externalIdentityRepository
            .findByProviderAndSubject(verifiedIdentity.provider, verifiedIdentity.subject)
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

    override fun reissue(refreshToken: String?): IssuedAuthTokens {
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

    override fun logout(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) return

        refreshTokenStore.delete(refreshToken)
    }
}
