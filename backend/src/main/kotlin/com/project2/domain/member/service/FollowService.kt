package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.dto.FollowResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.global.dto.RsData
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@RequiredArgsConstructor
class FollowService(
    private val followRepository: FollowRepository, private val memberRepository: MemberRepository, private val rq: Rq
) {
    @Transactional
    fun toggleFollow(requestDto: FollowRequestDto): RsData<FollowResponseDto> {
        val actor = rq.actor // 현재 사용자

        val following = requestDto.followingId?.let {
            memberRepository.findById(it).orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value().toString(), "팔로잉을 찾을 수 없습니다."
                )
            }
        }

        // 본인을 팔로우하는 것을 방지
        if (actor.id == following?.id) {
            return RsData("400", "본인을 팔로우할 수 없습니다.")
        }

        // followerId는 rq에서 가져오는 것으로 변경
        val follower = actor // 현재 사용자가 follower 역할을 함

        val existingFollow = followRepository.findByFollowerAndFollowing(follower, following)

        if (existingFollow!!.isPresent) {
            followRepository.delete(existingFollow.get())
            return RsData("204", "언팔로우 되었습니다.") // 언팔로우 시에는 응답 데이터가 없을 수 있음
        } else {
//            val newFollow = Follows(null, follower, following)
            val newFollow = Follows(null,follower,following)
            newFollow.follower = follower
            newFollow.following = following
            val savedFollow = followRepository.save(newFollow)

            val responseDto = FollowResponseDto(savedFollow)

//            responseDto.id= savedFollow.getId()!!
//            responseDto.followerId=savedFollow.getFollower()!!.getId()
//            responseDto.followingId=savedFollow.getFollowing()!!.getId()
            //안써도 문제없을듯

            return RsData("200", "팔로우 되었습니다.", responseDto)
        }
    }
}