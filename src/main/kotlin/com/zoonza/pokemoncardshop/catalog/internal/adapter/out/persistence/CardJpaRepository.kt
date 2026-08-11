package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.card.Card
import org.springframework.data.jpa.repository.JpaRepository

interface CardJpaRepository : JpaRepository<Card, Long>
