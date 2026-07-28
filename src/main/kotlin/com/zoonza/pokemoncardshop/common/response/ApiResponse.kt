package com.zoonza.pokemoncardshop.common.response

class ApiResponse<T> private constructor(
    val success: Boolean,
    val data: T?,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            success = true,
            data = data,
        )

        fun success(): ApiResponse<Unit> = ApiResponse(
            success = true,
            data = null,
        )

        fun <T> failure(error: T): ApiResponse<T> = ApiResponse(
            success = false,
            data = error,
        )
    }
}
