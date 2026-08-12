package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.common.error.DomainException

enum class CardRarity(val value: String) {
    ACE_SPEC_RARE("ACE SPEC Rare"),
    AMAZING_RARE("Amazing Rare"),
    BLACK_WHITE_RARE("Black White Rare"),
    CLASSIC_COLLECTION("Classic Collection"),
    COMMON("Common"),
    CROWN("Crown"),
    DOUBLE_RARE("Double rare"),
    FOUR_DIAMOND("Four Diamond"),
    FULL_ART_TRAINER("Full Art Trainer"),
    HOLO_RARE("Holo Rare"),
    HOLO_RARE_V("Holo Rare V"),
    HOLO_RARE_VMAX("Holo Rare VMAX"),
    HOLO_RARE_VSTAR("Holo Rare VSTAR"),
    HYPER_RARE("Hyper rare"),
    ILLUSTRATION_RARE("Illustration rare"),
    LEGEND("LEGEND"),
    MEGA_HYPER_RARE("Mega Hyper Rare"),
    NONE("None"),
    ONE_DIAMOND("One Diamond"),
    ONE_SHINY("One Shiny"),
    ONE_STAR("One Star"),
    PROMO("Promo"),
    RADIANT_RARE("Radiant Rare"),
    RARE("Rare"),
    RARE_HOLO("Rare Holo"),
    RARE_HOLO_LV_X("Rare Holo LV.X"),
    RARE_PRIME("Rare PRIME"),
    SECRET_RARE("Secret Rare"),
    SHINY_ULTRA_RARE("Shiny Ultra Rare"),
    SHINY_RARE("Shiny rare"),
    SHINY_RARE_V("Shiny rare V"),
    SHINY_RARE_VMAX("Shiny rare VMAX"),
    SPECIAL_ILLUSTRATION_RARE("Special illustration rare"),
    THREE_DIAMOND("Three Diamond"),
    THREE_STAR("Three Star"),
    TWO_DIAMOND("Two Diamond"),
    TWO_SHINY("Two Shiny"),
    TWO_STAR("Two Star"),
    ULTRA_RARE("Ultra Rare"),
    UNCOMMON("Uncommon"),
    ;

    companion object {
        fun from(value: String): CardRarity =
            CardRarity.entries.find { it.value == value }
                ?: throw DomainException(CardErrorCode.NOT_SUPPORTED_RARITY)
    }
}
