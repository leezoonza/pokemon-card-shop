package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Embeddable
data class Attack(
    @Column(nullable = false)
    val name: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    val cost: List<String>,

    @Column
    val effect: String?,

    @Column
    val damage: String?
) {
}