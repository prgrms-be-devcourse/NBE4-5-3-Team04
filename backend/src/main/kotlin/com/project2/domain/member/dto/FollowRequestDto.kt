package com.project2.domain.member.dto

import com.project2.domain.member.entity.Member

data class FollowRequestDto(
    var followerId: Long, var followingId: Long
) {
    companion object {
        fun from(member: Member): FollowRequestDto = FollowRequestDto(

            followerId = member.id!!,
            followingId = member.id!!,

            )
    }

}
