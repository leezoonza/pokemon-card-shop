package com.zoonza.pokemoncardshop.common

import com.zoonza.pokemoncardshop.PokemonCardShopApplication
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class CommonModuleTests {

    @Test
    fun `common 모듈은 공개 모듈로 인식된다`() {
        val commonModule = ApplicationModules.of(PokemonCardShopApplication::class.java)
            .getModuleByName("common")
            .orElseThrow()

        commonModule.isOpen.shouldBeTrue()
    }
}
