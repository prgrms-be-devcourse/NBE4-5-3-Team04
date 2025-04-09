package com.project2.domain.member.dto

import com.project2.domain.member.entity.Member

data class FollowerResponseDto(
    val userId: Long, val email: String, val nickname: String, val profileImageUrl: String, val totalPages: Int = 0
) {
    companion object {
        fun from(member: Member): FollowerResponseDto = FollowerResponseDto(

            userId=member.id!!,
            email=member.email,
            nickname=member.nickname,
            profileImageUrl = member.getProfileImageUrlOrDefaultUrl(),
             0
        )
    }
}






