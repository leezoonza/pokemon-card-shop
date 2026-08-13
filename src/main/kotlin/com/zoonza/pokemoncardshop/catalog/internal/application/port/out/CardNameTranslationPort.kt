package com.zoonza.pokemoncardshop.catalog.internal.application.port.out

interface CardNameTranslationPort {
    fun translate(englishName: String): String?
}
