package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.global.security.Rq
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@ExtendWith(MockitoExtension::class)
class FollowServiceTest {
    @Mock
    private lateinit var followRepository: FollowRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var rq: Rq

    @InjectMocks
    private lateinit var followService: FollowService

    private lateinit var follower: Member
    private lateinit var following: Member
    private lateinit var requestDto: FollowRequestDto

    @BeforeEach
    fun setUp() {
        follower = Member()
        follower!!.id = 1L

        following = Member()
        following!!.id = 2L

        val follows = Follows(null, follower, following)
        follows.follower = follower
        follows.following = following

        requestDto = FollowRequestDto()
        requestDto!!.followingId = following!!.id
    }

    @Test
    @DisplayName("팔로우 성공")
    fun testToggleFollow_Success_Follow() {
        // given
        Mockito.`when`(rq!!.actor).thenReturn(follower)
        Mockito.`when`(
            following!!.id?.let {
                memberRepository!!.findById(
                    it
                )
            }).thenReturn(Optional.of(following!!)) // following.getId() 사용
//        Mockito.`when`(followRepository!!.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty())
        val savedFollow = Follows(1L, follower, following)
        Mockito.`when`(followRepository?.save(ArgumentMatchers.any(Follows::class.java))).thenReturn(savedFollow)

        // when
        val response = followService!!.toggleFollow(requestDto!!)

        // then
        Assertions.assertEquals(1L, response.data.followerId)
        Assertions.assertEquals(2L, response.data.followingId)
    }

    @Test
    @DisplayName("언팔로우 성공")
    fun testToggleFollow_Success_Unfollow() {
        // 1. Given (준비)
        //   - 현재 사용자(follower) 설정
        Mockito.`when`(rq!!.actor).thenReturn(follower)

        //   - requestDto에 followingId 설정
        requestDto!!.followingId = following!!.id

        //   - 팔로잉 설정 (memberRepository.findById() 스텁)
        Mockito.`when`(
            following!!.id?.let {
                memberRepository!!.findById(
                    it
                )
            }).thenReturn(Optional.of(following!!))

        //   - 팔로우 관계가 이미 존재하는 경우 설정
        val existingFollow = Follows(1L, follower, following)
//        Mockito.`when`(followRepository!!.findByFollowerAndFollowing(follower, following))
//            .thenReturn(Optional.of(existingFollow))

        // 2. When (실행)
        //   - toggleFollow 메서드 실행
        val response = followService!!.toggleFollow(requestDto!!)

        // 3. Then (검증)
        //   - 응답 코드가 204인지 확인 (언팔로우 성공)
        org.assertj.core.api.Assertions.assertThat(response.code).isEqualTo("204")

        //   - followRepository.delete() 메서드가 호출되었는지 확인
        Mockito.verify(followRepository)?.delete(existingFollow)
    }
}