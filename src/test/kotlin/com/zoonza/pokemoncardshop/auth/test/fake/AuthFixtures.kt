package com.zoonza.pokemoncardshop.auth.test.fake

import com.zoonza.pokemoncardshop.auth.internal.application.dto.*
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import java.time.Duration
import java.time.Instant

val TEST_AUTHENTICATED_AT: Instant = Instant.parse("2026-08-02T03:00:00Z")
val TEST_REFRESH_TOKEN_TTL: Duration = Duration.ofDays(14)

fun signupCommandFixture(): SignupCommand = SignupCommand(
    nickname = "피카츄",
    identityTicket = "identity-ticket",
)

fun verifiedExternalIdentityFixture(): VerifiedExternalIdentity = VerifiedExternalIdentity(
    provider = IdentityProvider.GOOGLE,
    subject = "google-subject",
)

fun issuedAuthTokensFixture(
    accessToken: String = "access-token",
    refreshToken: String = "refresh-token",
): IssuedAuthTokens = IssuedAuthTokens(
    accessToken = IssuedAccessToken(accessToken),
    refreshToken = IssuedRefreshToken(refreshToken, TEST_REFRESH_TOKEN_TTL),
)

fun externalIdentityFixture(
    memberId: Long = 42L,
    identity: VerifiedExternalIdentity = verifiedExternalIdentityFixture(),
    createdAt: Instant = TEST_AUTHENTICATED_AT.minusSeconds(60),
): ExternalIdentity = ExternalIdentity.register(
    provider = identity.provider,
    subject = identity.subject,
    memberId = memberId,
    createdAt = createdAt,
)
