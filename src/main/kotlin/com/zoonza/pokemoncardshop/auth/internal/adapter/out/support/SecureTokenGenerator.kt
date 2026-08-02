package com.zoonza.pokemoncardshop.auth.internal.adapter.out.support

interface SecureTokenGenerator {
    fun generate(byteLength: Int): String
}