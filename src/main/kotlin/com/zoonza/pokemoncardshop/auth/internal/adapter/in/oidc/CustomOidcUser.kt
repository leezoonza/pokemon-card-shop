package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class CustomOidcUser(
    private val delegate: OidcUser,
    val provider: ExternalAccountProvider,
    private val subject: String
) : OidcUser {

    override fun getSubject(): String {
        return this.subject
    }

    override fun getClaims(): Map<String, Any> {
        return delegate.claims
    }

    override fun getUserInfo(): OidcUserInfo? {
        return delegate.userInfo
    }

    override fun getIdToken(): OidcIdToken {
        return delegate.idToken
    }

    override fun getAttributes(): Map<String, Any> {
        return delegate.attributes
    }

    override fun getAuthorities(): Collection<out GrantedAuthority> {
        return delegate.authorities
    }

    override fun getName(): String {
        return delegate.name
    }
}
