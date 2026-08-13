package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedRefreshToken
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupResult
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.ExternalAccountCommandUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenPort
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccount
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.member.api.MemberRegisterCommand
import com.zoonza.pokemoncardshop.member.api.MemberRegistrationApi
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExternalAccountCommandService(
    private val authTokenPort: AuthTokenPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val identityTicketPort: IdentityTicketPort,
    private val memberRegistrationApi: MemberRegistrationApi,
    private val externalAccountRepository: ExternalAccountRepository,
) : ExternalAccountCommandUseCase {

    @Transactional
    override fun signup(command: SignupCommand): SignupResult {
        val verifiedIdentity = identityTicketPort.consume(command.identityTicket)

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

        return SignupResult(authTokens, result.role)
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