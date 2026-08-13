package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.*
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.AuthenticationUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenPort
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccount
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberAuthenticationApi
import com.zoonza.pokemoncardshop.member.api.MemberLoginCommand
import com.zoonza.pokemoncardshop.member.api.MemberLoginResult
import com.zoonza.pokemoncardshop.member.api.MemberRoleResult
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger { }

@Service
class AuthenticationService(
    private val authTokenPort: AuthTokenPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val identityTicketPort: IdentityTicketPort,
    private val memberAuthenticationApi: MemberAuthenticationApi,
    private val externalAccountRepository: ExternalAccountRepository
) : AuthenticationUseCase {

    @Transactional
    override fun login(identityTicket: String, loggedInAt: Instant): LoginResult {
        val verifiedIdentity = identityTicketPort.consume(identityTicket)

        val externalAccount = findExternalAccount(verifiedIdentity)

        val command = MemberLoginCommand(externalAccount.memberId, loggedInAt)
        val result = loginMember(command)

        val authTokens = issueAuthTokens(externalAccount.memberId, result.role)

        return LoginResult(authTokens, result.role)
    }

    override fun refresh(refreshToken: String?): RefreshResult {
        val token = requireRefreshToken(refreshToken)

        val memberId = refreshTokenPort.consume(token)

        val result = getMemberRole(memberId)

        val authTokens = authTokenPort.issue(memberId, result.role)

        saveRefreshToken(memberId, authTokens.refreshToken)

        return RefreshResult(authTokens, result.role)
    }

    override fun logout(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) return

        refreshTokenPort.delete(refreshToken)
    }

    private fun findExternalAccount(verifiedIdentity: VerifiedExternalIdentity): ExternalAccount =
        externalAccountRepository
            .findByProviderAndSubject(verifiedIdentity.provider, verifiedIdentity.subject)
            ?: authenticationFailed("로그인 실패 opertaion=login reason=external_account_not_found")

    private fun loginMember(command: MemberLoginCommand): MemberLoginResult =
        memberAuthenticationApi.login(command)
            ?: authenticationFailed("로그인 실패 opertaion=login reason=liked_member_not_found")

    private fun requireRefreshToken(refreshToken: String?): String =
        refreshToken
            ?.takeIf { it.isNotBlank() }
            ?: throw DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

    private fun getMemberRole(memberId: Long): MemberRoleResult =
        memberAuthenticationApi.getMemberRole(memberId)
            ?: authenticationFailed("토큰 재발급 실패 operation=refresh reason=member_not_found")

    private fun authenticationFailed(message: String): Nothing {
        logger.warn { message }
        throw DomainException(AuthErrorCode.AUTHENTICATION_FAILED)
    }

    private fun issueAuthTokens(memberId: Long, role: String): IssuedAuthTokens {
        val authTokens = authTokenPort.issue(memberId, role)

        saveRefreshToken(memberId, authTokens.refreshToken)

        return authTokens
    }

    private fun saveRefreshToken(
        memberId: Long,
        issuedRefreshToken: IssuedRefreshToken,
    ) {
        refreshTokenPort.save(
            memberId = memberId,
            refreshToken = issuedRefreshToken.value,
            ttl = issuedRefreshToken.ttl
        )
    }
}
