package com.zoonza.pokemoncardshop.member.internal.application.dto

import com.zoonza.pokemoncardshop.member.internal.domain.Nickname

data class UpdateNicknameCommand(
    val memberId: Long,
    val nickname: Nickname
) {
    constructor(
        memberId: Long,
        nickname: String
    ) : this(
        memberId = memberId,
        nickname = Nickname(nickname)
    )
}
