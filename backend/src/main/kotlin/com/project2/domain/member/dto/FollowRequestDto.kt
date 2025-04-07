package com.project2.domain.member.dto

import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
class FollowRequestDto {
    var followerId: Long? = null
    var followingId: Long? = null

//    public FollowRequestDto(Follows follows) {
    //
    //        this.followerId = follows.getFollower().getId();
    //        this.followingId = follows.getFollowing().getId();
    //    }
}
