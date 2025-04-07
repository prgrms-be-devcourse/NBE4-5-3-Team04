package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowerResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function

@Service
@RequiredArgsConstructor
class FollowerService {
    private val followRepository: FollowRepository? = null
    private val memberService: MemberService? = null
    private val rq: Rq? = null

    fun getFollowers(memberId: Long, pageable: Pageable?): Page<FollowerResponseDto>? {
        val actor = rq!!.actor
        if (actor.id != memberId) {
            throw ServiceException("403", "자신의 팔로워 목록만 볼 수 있습니다.")
        }

        val member = memberService!!.findByIdOrThrow(memberId)

//        return followRepository!!.findByFollowing(member, pageable)
//            .map(Function { follow: Follows ->
//                FollowerResponseDto.fromEntity(
//                    follow.getFollower()!!
//                )
//            })
        return followRepository!!.findByFollowing(member, pageable)
            ?.map { follow ->
                FollowerResponseDto(follow?.follower!!)
            }
    }

    @Transactional(readOnly = true)
    fun getFollowersCount(member: Member?): Long {
        return followRepository!!.countByFollowing(member)
    }
}
