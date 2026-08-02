package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableConfigurationProperties(JwtTokenProperties::class)
class JwtTokenConfig {

    @Bean
    fun jwtEncoder(properties: JwtTokenProperties): JwtEncoder =
        NimbusJwtEncoder.withSecretKey(secretKey(properties))
            .algorithm(MacAlgorithm.HS256)
            .build()

    @Bean
    fun jwtDecoder(properties: JwtTokenProperties): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(secretKey(properties))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

    private fun secretKey(properties: JwtTokenProperties): SecretKeySpec =
        SecretKeySpec(
            properties.secret.toByteArray(Charsets.UTF_8),
            ALGORITHM
        )

    companion object {
        private const val ALGORITHM = "HmacSHA256"
    }
}