package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.tcgdex

import net.tcgdex.sdk.TCGdex
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TcgdexConfig {

    @Bean
    fun tcgdexClient(): TCGdex = TCGdex("en")
}
