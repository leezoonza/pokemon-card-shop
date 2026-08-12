package com.zoonza.pokemoncardshop.catalog.internal.application.port.out

interface CardNameTranslator {
    fun translate(englishName: String): String?
}
