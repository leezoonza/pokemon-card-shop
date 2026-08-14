package com.zoonza.pokemoncardshop.catalog.internal.domain.expansion

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ExpansionImage(
    @Column
    val logoUrl: String?,

    @Column
    val symbolUrl: String?
)
