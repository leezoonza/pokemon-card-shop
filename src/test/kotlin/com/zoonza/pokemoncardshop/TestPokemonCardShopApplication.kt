package com.zoonza.pokemoncardshop

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<PokemonCardShopApplication>().with(TestcontainersConfiguration::class).run(*args)
}
