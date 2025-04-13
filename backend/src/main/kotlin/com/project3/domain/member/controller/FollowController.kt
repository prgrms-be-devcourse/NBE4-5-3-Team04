package com.project3.domain.member.controller

import com.project3.domain.member.dto.FollowRequestDto
import com.project3.domain.member.dto.FollowResponseDto
import com.project3.domain.member.dto.FollowerResponseDto
import com.project3.domain.member.service.FollowService
import com.project3.domain.member.service.FollowerService
import com.project3.domain.member.service.FollowingService
import com.project3.global.dto.RsData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/follows")

class FollowController(
        private val followService: FollowService,
        private val followerService: FollowerService,
        private val followingService: FollowingService,
) {
    @PostMapping("/{memberid}/follows")
    fun toggleFollow(
            @PathVariable memberid: Long, @RequestBody requestDto: FollowRequestDto
    ): RsData<FollowResponseDto> {
        requestDto.followerId = memberid
        return followService.toggleFollow(requestDto)
    }

    @GetMapping("/{memberId}/followers")
    fun getFollowers(
            @PathVariable memberId: Long,
            @PageableDefault(size = 8) pageable: Pageable
    ): RsData<Page<FollowerResponseDto>> {
        val followers = followerService.getFollowers(memberId, pageable)

        return if (followers.isEmpty) {
            RsData("204", "팔로워가 없습니다.")
        } else {
            RsData("200", "팔로워 목록이 성공적으로 조회되었습니다.", followers)
        }
    }

    @GetMapping("/{memberId}/followings")
    fun getFollowings(@PathVariable memberId: Long): RsData<List<FollowerResponseDto>> {
        val followings = followingService.getFollowings(memberId)

        return if (followings.isEmpty()) {
            RsData("204", "팔로잉이 없습니다.")
        } else {
            RsData("200", "팔로잉 목록이 성공적으로 조회되었습니다.", followings)
        }
    }
}