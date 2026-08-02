package com.zoonza.pokemoncardshop.auth.internal.adapter.out.support

import org.springframework.stereotype.Component
import java.security.SecureRandom
import kotlin.io.encoding.Base64

@Component
class SecureRandomTokenGenerator : SecureTokenGenerator {
    private val secureRandom = SecureRandom()

    override fun generate(byteLength: Int): String {
        val bytes = ByteArray(byteLength)

        secureRandom.nextBytes(bytes)

        return Base64.UrlSafe
            .withPadding(Base64.PaddingOption.ABSENT)
            .encode(bytes)
    }
}