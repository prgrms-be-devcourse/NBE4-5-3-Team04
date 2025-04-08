package com.project2.domain.member.dto

import com.project2.domain.member.entity.Member

data class AuthorDTO(val memberId: Long, val nickname: String, val profileImageUrl: String) {
    companion object {
        fun from(member: Member): AuthorDTO = AuthorDTO(memberId = member.id!!, nickname = member.nickname, profileImageUrl = member.getProfileImageUrlOrDefaultUrl())
    }
}
