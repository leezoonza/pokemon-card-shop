package com.zoonza.pokemoncardshop.global.security

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties::class)
class SecurityConfig(
    private val corsProperties: CorsProperties,
    @Qualifier("customOidcUserService")
    private val oidcUserService: OidcUserService,
    @Qualifier("oidcAuthenticationSuccessHandler")
    private val oidcAuthenticationSuccessHandler: AuthenticationSuccessHandler,
    @Qualifier("oidcAuthenticationFailureHandler")
    private val oidcAuthenticationFailureHandler: AuthenticationFailureHandler,
    private val jwtAuthenticationConverter: JwtAuthenticationConverter,
    private val apiAuthenticationEntryPoint: ApiAuthenticationEntryPoint,
    private val apiAccessDeniedHandler: ApiAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { }
            .csrf { it.spa() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/**",
                    "/api/auth/**",
                    "/api/members/nickname",
                    "/error",
                ).permitAll()
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN")
                auth.anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(apiAuthenticationEntryPoint)
                exceptions.accessDeniedHandler(apiAccessDeniedHandler)
            }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.oidcUserService(oidcUserService)
                }
                oauth2.successHandler(oidcAuthenticationSuccessHandler)
                oauth2.failureHandler(oidcAuthenticationFailureHandler)
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.authenticationEntryPoint(apiAuthenticationEntryPoint)
                resourceServer.accessDeniedHandler(apiAccessDeniedHandler)
                resourceServer.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }
            .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(corsProperties.allowedOrigin)
            allowedMethods = listOf(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS",
            )
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "X-XSRF-TOKEN",
            )
            allowCredentials = true
            maxAge = 3600
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
