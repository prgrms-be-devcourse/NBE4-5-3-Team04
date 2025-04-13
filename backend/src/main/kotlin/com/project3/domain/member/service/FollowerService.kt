package com.project3.domain.member.service

import com.project3.domain.member.dto.FollowerResponseDto
import com.project3.domain.member.entity.Member
import com.project3.domain.member.repository.FollowRepository
import com.project3.global.exception.ServiceException
import com.project3.global.security.Rq

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class FollowerService(
    private val followRepository: FollowRepository, private val memberService: MemberService, private val rq: Rq
) {
    fun getFollowers(memberId: Long, pageable: Pageable): Page<FollowerResponseDto> {
        val actor = rq.getActor()
        if (actor.id != memberId) {
            throw ServiceException("403", "자신의 팔로워 목록만 볼 수 있습니다.")
        }

        val member = memberService.findByIdOrThrow(memberId)


        return followRepository.findByFollowing(member, pageable).map { follow ->
            FollowerResponseDto.from(follow.follower!!)
        }
    }

    @Transactional(readOnly = true)
    fun getFollowersCount(member: Member): Long {
        return followRepository.countByFollowing(member)
    }
}
