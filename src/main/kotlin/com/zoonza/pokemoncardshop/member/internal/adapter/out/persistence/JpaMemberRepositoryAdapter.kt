package com.zoonza.pokemoncardshop.member.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaMemberRepositoryAdapter(
    private val repository: MemberJpaRepository
) : MemberRepository {

    override fun existsByNickname(nickname: Nickname): Boolean {
        return repository.existsByNickname(nickname)
    }

    override fun save(member: Member): Member {
        return repository.save(member)
    }

    override fun findByIdOrThrow(id: Long): Member {
        return repository.findByIdOrNull(id)
            ?: throw DomainException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    override fun findByIdOrNull(id: Long): Member? {
        return repository.findByIdOrNull(id)
    }
}