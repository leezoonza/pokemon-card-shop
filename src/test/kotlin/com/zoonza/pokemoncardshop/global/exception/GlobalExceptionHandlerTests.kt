package com.zoonza.pokemoncardshop.global.exception

import com.zoonza.pokemoncardshop.common.error.CommonErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.common.error.ValidationError
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTests {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `도메인 예외를 오류 코드의 상태와 응답으로 변환한다`() {
        val response = handler.handleDomainException(
            DomainException(CommonErrorCode.VALIDATION_FAILED),
        )

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.success shouldBe false
        response.body?.data?.code shouldBe CommonErrorCode.VALIDATION_FAILED.code
        response.body?.data?.message shouldBe CommonErrorCode.VALIDATION_FAILED.message
    }

    @Test
    @ExtendWith(OutputCaptureExtension::class)
    fun `원인이 있는 도메인 예외는 원인을 경고로 기록한다`(output: CapturedOutput) {
        val cause = IllegalStateException("translated-cause")

        handler.handleDomainException(
            DomainException(CommonErrorCode.VALIDATION_FAILED, cause),
        )

        output.toString() shouldContain "WARN"
        output.toString() shouldContain "translated-cause"
        output.toString() shouldNotContain "IllegalStateException"
    }

    @Test
    fun `요청 검증 예외의 필드 오류와 전역 오류를 응답에 포함한다`() {
        val target = ValidationTarget(name = null)
        val bindingResult = BeanPropertyBindingResult(target, "target").apply {
            addError(
                FieldError(
                    "target",
                    "name",
                    null,
                    false,
                    null,
                    null,
                    "이름은 필수입니다.",
                ),
            )
            addError(ObjectError("target", "요청 값 조합이 올바르지 않습니다."))
        }
        val exception = MethodArgumentNotValidException(methodParameter(), bindingResult)

        val response = handler.handleMethodArgumentNotValid(exception)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.success shouldBe false
        response.body?.data?.errors shouldContainExactly listOf(
            ValidationError("name", "이름은 필수입니다."),
            ValidationError(null, "요청 값 조합이 올바르지 않습니다."),
        )
    }

    @Test
    fun `처리되지 않은 예외를 내부 서버 오류 응답으로 변환한다`() {
        val response = handler.handleUnexpectedException(
            IllegalStateException("unexpected"),
        )

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.success shouldBe false
        response.body?.data?.code shouldBe CommonErrorCode.INTERNAL_SERVER_ERROR.code
        response.body?.data?.message shouldBe CommonErrorCode.INTERNAL_SERVER_ERROR.message
    }

    @Suppress("UNUSED_PARAMETER")
    private fun validationMethod(target: ValidationTarget) = Unit

    private fun methodParameter(): MethodParameter = MethodParameter(
        GlobalExceptionHandlerTests::class.java.getDeclaredMethod(
            "validationMethod",
            ValidationTarget::class.java,
        ),
        0,
    )

    private data class ValidationTarget(
        val name: String?,
    )
}
