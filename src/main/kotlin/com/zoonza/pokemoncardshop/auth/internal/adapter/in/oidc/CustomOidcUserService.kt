package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component

@Component
class CustomOidcUserService : OidcUserService() {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val provider = resolveProvider(userRequest)

        val subject = requireSubject(oidcUser)

        return CustomOidcUser(
            oidcUser,
            provider,
            subject
        )
    }

    private fun resolveProvider(userRequest: OidcUserRequest): IdentityProvider =
        try {
            val registrationId = userRequest
                .clientRegistration
                .registrationId

            return IdentityProvider.from(registrationId)
        } catch (ex: DomainException) {
            throw OAuth2AuthenticationException(
                OAuth2Error(
                    "unsupported_oidc_provider",
                    "지원하지 않는 OIDC 제공자 입니다.",
                    null
                )
            )
        }

    private fun requireSubject(oidcUser: OidcUser): String =
        oidcUser.subject
            ?: throw OAuth2AuthenticationException(
                OAuth2Error(
                    "invalid_id_token",
                    "OIDC 사용자 식별자가 없습니다.",
                    null
                )
            )
}
