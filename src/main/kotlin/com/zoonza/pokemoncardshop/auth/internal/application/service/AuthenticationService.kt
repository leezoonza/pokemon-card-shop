package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthenticationResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedRefreshToken
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.Authenticator
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccount
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthenticationService(
    private val authTokenIssuer: AuthTokenIssuer,
    private val refreshTokenStore: RefreshTokenStore,
    private val identityTicketStore: IdentityTicketStore,
    private val memberRegistrationApi: MemberRegistrationApi,
    private val memberAuthenticationApi: MemberAuthenticationApi,
    private val externalAccountRepository: ExternalAccountRepository
) : Authenticator {

    @Transactional
    override fun signup(command: SignupCommand): AuthenticationResult {
        val verifiedIdentity = identityTicketStore.consume(command.identityTicket)

        val memberRegisterCommand = MemberRegisterCommand(command.nickname, command.createdAt)
        val result = memberRegistrationApi.register(memberRegisterCommand)

        val externalAccount = ExternalAccount.register(
            provider = verifiedIdentity.provider,
            subject = verifiedIdentity.subject,
            memberId = result.memberId,
            linkedAt = command.createdAt
        )

        externalAccountRepository.save(externalAccount)

        val authTokens = issueAuthTokens(result.memberId, result.role)

        return AuthenticationResult(authTokens, result.role)
    }

    @Transactional
    override fun login(identityTicket: String, loggedInAt: Instant): AuthenticationResult {
        val verifiedIdentity = identityTicketStore.consume(identityTicket)

        val externalAccount = externalAccountRepository
            .findByProviderAndSubject(verifiedIdentity.provider, verifiedIdentity.subject)
            ?: throw DomainException(AuthErrorCode.AUTHENTICATION_FAILED)

        val command = MemberLoginCommand(externalAccount.memberId, loggedInAt)
        val result = loginMember(command)

        val authTokens = issueAuthTokens(externalAccount.memberId, result.role)

        return AuthenticationResult(authTokens, result.role)
    }

    override fun reissue(refreshToken: String?): AuthenticationResult {
        val token = refreshToken
            ?.takeIf { it.isNotBlank() }
            ?: throw DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val memberId = refreshTokenStore.consume(token)

        val result = getMemberRole(memberId)

        val authTokens = authTokenIssuer.issue(memberId, result.role)

        saveRefreshToken(memberId, authTokens.refreshToken)

        return AuthenticationResult(authTokens, result.role)
    }

    override fun logout(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) return

        refreshTokenStore.delete(refreshToken)
    }

    private fun loginMember(command: MemberLoginCommand): MemberLoginResult {
        return try {
            memberAuthenticationApi.login(command)
        } catch (exception: DomainException) {
            throw DomainException(AuthErrorCode.AUTHENTICATION_FAILED, exception)
        }
    }

    private fun getMemberRole(memberId: Long): MemberRoleResult {
        return try {
            memberAuthenticationApi.getMemberRole(memberId)
        } catch (exception: DomainException) {
            throw DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN, exception)
        }
    }

    private fun issueAuthTokens(memberId: Long, role: String): IssuedAuthTokens {
        val authTokens = authTokenIssuer.issue(memberId, role)

        saveRefreshToken(memberId, authTokens.refreshToken)

        return authTokens
    }

    private fun saveRefreshToken(
        memberId: Long,
        issuedRefreshToken: IssuedRefreshToken,
    ) {
        refreshTokenStore.save(
            memberId = memberId,
            refreshToken = issuedRefreshToken.value,
            ttl = issuedRefreshToken.ttl
        )
    }
}
