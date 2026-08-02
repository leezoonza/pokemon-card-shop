package com.zoonza.pokemoncardshop

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModulithArchitectureTests {

    @Test
    fun `모듈 경계와 순환 의존성을 검증한다`() {
        ApplicationModules.of(PokemonCardShopApplication::class.java).verify()
    }
}
