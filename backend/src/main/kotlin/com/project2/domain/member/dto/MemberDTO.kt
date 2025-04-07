package com.project2.domain.member.dto

import com.project2.domain.member.entity.Member

data class MemberDTO(
        val id: Long,
        val nickname: String,
        val profileImageUrl: String
) {
    companion object {
        fun from(member: Member): MemberDTO = MemberDTO(
                id = member.id!!,
                nickname = member.nickname,
                profileImageUrl = member.getProfileImageUrlOrDefaultUrl()
        )
    }
}