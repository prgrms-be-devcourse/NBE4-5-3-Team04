package com.project2.domain.post.service

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.dto.FollowResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.post.dto.toggle.LikeResponseDTO
import com.project2.domain.post.dto.toggle.ScrapResponseDTO
import com.project2.domain.post.mapper.ToggleMapper
import com.project2.domain.post.repository.LikesRepository
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.post.repository.ScrapRepository
import com.project2.global.dto.RsData
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostToggleService(
    private val likesRepository: LikesRepository,
    private val scrapRepository: ScrapRepository,
    private val postRepository: PostRepository,
    private val toggleMapper: ToggleMapper,
    private val followRepository: FollowRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun toggleLikes(userId: Long?, postId: Long?): RsData<LikeResponseDTO> {
        val isLiked = likesRepository.existsByPostIdAndMemberId(postId, userId)

        if (isLiked) {
            likesRepository.toggleLikeIfExists(postId, userId)
        } else {
            likesRepository.save(toggleMapper.toLikes(userId!!, postId!!))
        }

        val responseDTO = LikeResponseDTO(!isLiked, likesRepository.countByPostId(postId))
        return RsData("200", "좋아요 상태 변경 완료", responseDTO)
    }

    @Transactional
    fun toggleScrap(userId: Long?, postId: Long): RsData<ScrapResponseDTO> {
        val isScrapped = scrapRepository.existsByPostIdAndMemberId(postId, userId)

        if (isScrapped) {
            scrapRepository.toggleScrapIfExists(postId, userId)
        } else {
            val post = postRepository.getReferenceById(postId)
            scrapRepository.save(toggleMapper.toScrap(userId!!, post))
        }

        val responseDTO = ScrapResponseDTO(!isScrapped, scrapRepository.countByPostId(postId))
        return RsData("200", "스크랩 상태 변경 완료", responseDTO)
    }

    @Transactional
    fun toggleFollow(requestDto: FollowRequestDto): RsData<FollowResponseDto> {
        val followerId = requestDto.followerId
        val followingId = requestDto.followingId

        val follower = memberRepository.findById(followerId)
            .orElseThrow { IllegalArgumentException("팔로워를 찾을 수 없습니다.") }
        val following = memberRepository.findById(followingId)
            .orElseThrow { IllegalArgumentException("팔로잉 사용자를 찾을 수 없습니다.") }

        val isFollowing = followRepository.existsByFollowerAndFollowing(follower, following)

        val follows = Follows(
            id = null,
            follower = follower,
            following = following
        )

        if (isFollowing) {
            followRepository.deleteByFollowerAndFollowing(follower, following)
        } else {
            follows.follower = follower
            follows.following = following
            followRepository.save(follows)
        }

        val responseDto = FollowResponseDto(follows) // 생성된 Follows 객체 전달
        return RsData("200", "팔로우 상태 변경 완료", responseDto)
    }
}