package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowerResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service

class FollowingService(
    private val followRepository: FollowRepository, private val memberService: MemberService, private val rq: Rq?
) {
    fun getFollowings(memberId: Long): MutableList<FollowerResponseDto> {
        val actor = rq!!.actor

        if (actor.id != memberId) {
            throw ServiceException("403", "자신의 팔로잉 목록만 볼 수 있습니다.")
        }
        val member = memberService.findByIdOrThrow(memberId)
        val followsList = followRepository.findByFollower(member)

        return followsList.stream().map { follow: Follows -> FollowerResponseDto.from(follow.following!!) }
            .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    fun getFollowingsCount(member: Member): Long {
        return followRepository.countByFollower(member)
    }
}
