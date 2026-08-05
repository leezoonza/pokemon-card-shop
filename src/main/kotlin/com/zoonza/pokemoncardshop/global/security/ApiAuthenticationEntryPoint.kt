package com.zoonza.pokemoncardshop.global.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ApiAuthenticationEntryPoint(
    private val responseWriter: ApiSecurityErrorResponseWriter,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        logger.warn { authException.message }

        responseWriter.write(
            response,
            SecurityErrorCode.AUTHENTICATION_REQUIRED,
        )
    }
}
