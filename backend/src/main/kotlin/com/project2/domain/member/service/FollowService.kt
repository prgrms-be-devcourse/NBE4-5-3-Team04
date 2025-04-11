package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.dto.FollowResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.event.NotificationEvent
import com.project2.domain.notification.service.NotificationService
import com.project2.global.dto.RsData
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class FollowService(
        private val followRepository: FollowRepository,
        private val memberRepository: MemberRepository,
        private val rq: Rq,
        private val notificationService: NotificationService
) {
    @Transactional
    fun toggleFollow(requestDto: FollowRequestDto): RsData<FollowResponseDto> {
        val actor = rq.getActor() // 현재 사용자
        val follower = memberRepository.findById(actor.id!!).orElseThrow {
            ServiceException(
                    HttpStatus.NOT_FOUND.value().toString(), "회원 정보를 찾을 수 없습니다."
            )
        }

        val following = requestDto.followingId.let {
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

        val existingFollow = following?.let { followRepository.findByFollowerAndFollowing(actor, it) }

        if (existingFollow!!.isPresent) {
            followRepository.delete(existingFollow.get())
            return RsData("204", "언팔로우 되었습니다.") // 언팔로우 시에는 응답 데이터가 없을 수 있음
        } else {

            val newFollow = Follows(null, actor, following)
            newFollow.follower = actor
            newFollow.following = following
            val savedFollow = followRepository.save(newFollow)

            // 팔로우 알림 생성
            val event = NotificationEvent(
                    receiver = following,
                    sender = actor,
                    type = NotificationType.NEW_FOLLOWER,
                    content = "${follower.nickname}님이 팔로우하기 시작했습니다.",
                    relatedId = actor.id!! // 팔로우한 사용자 ID
            )
            // 비동기 처리를 위해 notificationService 사용
            notificationService.processNotificationAsync(event)

            val responseDto = FollowResponseDto(savedFollow)

            return RsData("200", "팔로우 되었습니다.", responseDto)
        }
    }
}