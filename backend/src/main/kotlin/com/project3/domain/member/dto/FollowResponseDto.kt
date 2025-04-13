package com.project3.domain.member.dto

import com.project3.domain.member.entity.Follows

class FollowResponseDto(follows: Follows) {
    val id = follows.id
    val followerId = follows.follower?.id
    val followingId = follows.following?.id

}