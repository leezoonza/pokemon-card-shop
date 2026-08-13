package com.zoonza.pokemoncardshop.global.exception

import com.zoonza.pokemoncardshop.common.error.CommonErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.common.error.ValidationError
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import com.zoonza.pokemoncardshop.common.response.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        exception: DomainException,
    ): ResponseEntity<ApiResponse<ErrorResponse>> {
        val errorCode = exception.errorCode
        val error = ErrorResponse.of(errorCode)

        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.failure(error))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ApiResponse<ErrorResponse>> {
        val fieldErrors = exception.bindingResult.fieldErrors.map { error ->
            ValidationError(
                field = error.field,
                message = error.defaultMessage,
            )
        }
        val globalErrors = exception.bindingResult.globalErrors.map { error ->
            ValidationError(
                field = null,
                message = error.defaultMessage,
            )
        }
        val response = ErrorResponse.validation(
            errorCode = CommonErrorCode.VALIDATION_FAILED,
            errors = fieldErrors + globalErrors,
        )

        return ResponseEntity
            .badRequest()
            .body(ApiResponse.failure(response))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        exception: Exception,
    ): ResponseEntity<ApiResponse<ErrorResponse>> {
        logger.error(exception) { "예상하지 못한 예외가 발생했습니다." }

        val error = ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR)

        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.failure(error))
    }
}
