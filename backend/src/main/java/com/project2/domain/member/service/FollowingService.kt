package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowerResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class FollowingService {
    private val followRepository: FollowRepository? = null
    private val memberService: MemberService? = null
    private val rq: Rq? = null

    fun getFollowings(memberId: Long): List<FollowerResponseDto> {
        val actor = rq!!.actor

        //		System.out.println("aaaaaaaaactor = " + actor.getId()+" "+actor.getEmail());
        if (actor.id != memberId) {
            throw ServiceException("403", "자신의 팔로잉 목록만 볼 수 있습니다.")
        }
        val member = memberService!!.findByIdOrThrow(memberId)
        val followsList = followRepository!!.findByFollower(member)

        return followsList!!.stream()
            .map { follow: Follows? -> FollowerResponseDto(follow!!.following!!) }
            .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    fun getFollowingsCount(member: Member?): Long {
        return followRepository!!.countByFollower(member)
    }
}
