package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth.redirect")
class RedirectProperties(
    val signupUri: String,
    val loginUri: String,
    val failureUri: String
)