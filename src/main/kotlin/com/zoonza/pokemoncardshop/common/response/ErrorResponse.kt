package com.zoonza.pokemoncardshop.common.response

import com.zoonza.pokemoncardshop.common.error.ErrorCode
import com.zoonza.pokemoncardshop.common.error.ValidationError

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<ValidationError>,
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            errors = emptyList(),
        )

        fun validation(
            errorCode: ErrorCode,
            errors: List<ValidationError>,
        ): ErrorResponse = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            errors = errors.toList(),
        )
    }
}
