package com.project2.domain.member.dto

import com.project2.domain.member.entity.Follows

class FollowResponseDto(follows: Follows) {
    val id = follows.id
    val followerId = follows.follower?.id
    val followingId = follows.following?.id

}