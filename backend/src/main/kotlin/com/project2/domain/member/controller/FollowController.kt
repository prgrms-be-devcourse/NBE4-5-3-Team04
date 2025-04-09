package com.project2.domain.member.controller

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.dto.FollowResponseDto
import com.project2.domain.member.dto.FollowerResponseDto
import com.project2.domain.member.service.FollowService
import com.project2.domain.member.service.FollowerService
import com.project2.domain.member.service.FollowingService
import com.project2.domain.post.service.PostService
import com.project2.global.dto.RsData
import com.project2.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/follows")

class FollowController(
    private val followService: FollowService,
    private val followerService: FollowerService,
    private val followingService: FollowingService,
    private val postService: PostService
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
        @PathVariable memberId: Long?, @PageableDefault(size = 8) pageable: Pageable
    ): ResponseEntity<RsData<Page<FollowerResponseDto>>> {
        val followers = memberId?.let { followerService.getFollowers(it, pageable) }!!

        if (followers != null) {
            if (followers.isEmpty) {
                return ResponseEntity.noContent().build()
            }
        }

        println("가져온 팔로워 페이지 수: " + followers.totalPages)
        println("가져온 팔로워 명 수: " + followers.totalElements)

        return ResponseEntity.ok(
            RsData(
                "200", "팔로워 목록이 성공적으로 조회되었습니다.", followers
            )
        )
    }

    @GetMapping("/{memberId}/followings")
    fun getFollowings(@PathVariable memberId: Long?): ResponseEntity<RsData<List<FollowerResponseDto>>> {
        try {
            val followings = followingService.getFollowings(memberId!!)

            // Check if the list of followings is empty
            if (followings.isEmpty()) {
                return ResponseEntity.noContent().build()
            }

            return ResponseEntity.ok(
                RsData(
                    "200", "팔로잉 목록이 성공적으로 조회되었습니다.", followings
                )
            )
        } catch (e: ServiceException) {
            return ResponseEntity.noContent().build()
        }
    }


}