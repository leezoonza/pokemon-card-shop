package com.zoonza.pokemoncardshop.global.config

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.ZoneId

class TimeConfigTests {

    @Test
    fun `서울 시간대의 시스템 시계를 제공한다`() {
        val clock = TimeConfig().clock()

        clock.zone shouldBe ZoneId.of("Asia/Seoul")
    }
}
