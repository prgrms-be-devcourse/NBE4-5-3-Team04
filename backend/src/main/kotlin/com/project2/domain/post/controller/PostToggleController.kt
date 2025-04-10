package com.project2.domain.post.controller

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.dto.FollowResponseDto
import com.project2.domain.post.dto.toggle.LikeResponseDTO
import com.project2.domain.post.dto.toggle.ScrapResponseDTO
import com.project2.domain.post.service.PostToggleService
import com.project2.global.dto.RsData
import com.project2.global.security.SecurityUser
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostToggleController(
    private val postToggleService: PostToggleService
) {

    @PostMapping("/{postId}/likes")
    fun toggleLike(
        @AuthenticationPrincipal securityUser: SecurityUser,
        @PathVariable postId: Long?
    ): RsData<LikeResponseDTO> {
        return postToggleService.toggleLikes(securityUser.id, postId)
    }

    @PostMapping("/{postId}/scrap")
    fun toggleScrap(
        @AuthenticationPrincipal securityUser: SecurityUser,
        @PathVariable postId: Long?
    ): RsData<ScrapResponseDTO> {
        return postToggleService.toggleScrap(securityUser.id, postId!!)
    }

    @PostMapping("/{postId}/follow")
    fun toggleFollow(
        @AuthenticationPrincipal securityUser: SecurityUser,
        @PathVariable postId: Long?,
        @RequestBody requestDto: FollowRequestDto
    ): RsData<FollowResponseDto> {
        // 현재 로그인한 사용자의 ID를 followerId로 설정
        requestDto.followerId = securityUser.id
        // followingId는 requestDto에 포함되어 있어야 함
        return postToggleService.toggleFollow(requestDto)
    }
}