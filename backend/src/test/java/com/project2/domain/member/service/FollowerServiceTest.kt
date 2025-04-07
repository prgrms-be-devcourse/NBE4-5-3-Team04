package com.project2.domain.member.service

import com.project2.domain.member.dto.FollowerResponseDto
import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.global.exception.ServiceException
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*

@ExtendWith(MockitoExtension::class)
class FollowerServiceTest {
    @Mock
    private val followRepository: FollowRepository? = null

    @Mock
    private val memberService: MemberService? = null

    @Mock
    private val rq: Rq? = null

    @InjectMocks
    private val followerService: FollowerService? = null

    private var user: Member? = null
    private var follower1: Member? = null
    private var follower2: Member? = null

    @BeforeEach
    fun setUp() {
        // Create test members
        user = Member()
        user!!.id = 1L
        user!!.nickname = "testUser"

        follower1 = Member()
        follower1!!.id = 2L
        follower1!!.nickname = "follower1"

        follower2 = Member()
        follower2!!.id = 3L
        follower2!!.nickname = "follower2"
    }

    @Test
    fun testGetFollowers_Success() {
        Mockito.`when`(rq!!.actor).thenReturn(user)
        Mockito.`when`(memberService!!.findByIdOrThrow(1L)).thenReturn(user)

        val follow1 = Follows(null,follower1,user)
        follow1.follower = follower1
        follow1.following = user

        val follow2 = Follows(null,follower2,user)
        follow2.follower = follower2
        follow2.following = user

        val followsList = Arrays.asList(follow1, follow2)
        val followsPage: Page<Follows?> = PageImpl(followsList)

        Mockito.`when`(
            followRepository!!.findByFollowing(
                ArgumentMatchers.eq(user), ArgumentMatchers.any(
                    Pageable::class.java
                )
            )
        ).thenReturn(followsPage)

        val followers = followerService!!.getFollowers(1L, Pageable.unpaged())

        Assertions.assertNotNull(followers)
        Assertions.assertEquals(2, followers!!.content.size)

        Assertions.assertTrue(followers.content.stream().anyMatch { f: FollowerResponseDto ->
            f.getId()!!.equals(
                follower1!!.getId()
            )
        })
        Assertions.assertTrue(followers.content.stream().anyMatch { f: FollowerResponseDto ->
            f.getId()!!.equals(
                follower2!!.getId()
            )
        })

        Mockito.verify(rq).actor
        Mockito.verify(memberService).findByIdOrThrow(1L)
        Mockito.verify(followRepository).findByFollowing(
            ArgumentMatchers.eq(user), ArgumentMatchers.any(
                Pageable::class.java
            )
        )
    }

    @Test
    fun testGetFollowers_NoFollowers() {
        Mockito.`when`(rq!!.actor).thenReturn(user)
        Mockito.`when`(memberService!!.findByIdOrThrow(1L)).thenReturn(user)

        val emptyPage = Page.empty<Follows?>()
        Mockito.`when`(
            followRepository!!.findByFollowing(
                ArgumentMatchers.eq(user), ArgumentMatchers.any(
                    Pageable::class.java
                )
            )
        ).thenReturn(emptyPage)

        val followers = followerService!!.getFollowers(1L, Pageable.unpaged())

        Assertions.assertNotNull(followers)
        Assertions.assertTrue(followers!!.isEmpty)

        Mockito.verify(rq).actor
        Mockito.verify(memberService).findByIdOrThrow(1L)
        Mockito.verify(followRepository).findByFollowing(
            ArgumentMatchers.eq(user), ArgumentMatchers.any(
                Pageable::class.java
            )
        )
    }

    @Test
    fun testGetFollowers_UserNotFound() {
        Mockito.`when`(rq!!.actor).thenReturn(user)
        Mockito.`when`(memberService!!.findByIdOrThrow(1L)).thenThrow(ServiceException("404", "사용자를 찾을 수 없습니다."))

        val exception = Assertions.assertThrows(
            ServiceException::class.java
        ) {
            followerService!!.getFollowers(1L, Pageable.unpaged())
        }

        Assertions.assertEquals("404", exception.code)
        Assertions.assertEquals("사용자를 찾을 수 없습니다.", exception.message)

        Mockito.verify(rq).actor
        Mockito.verify(memberService).findByIdOrThrow(1L)
    }

    @Test
    @DisplayName("회원이 존재할 경우 팔로워 수를 정상적으로 반환해야 한다")
    fun testGetFollowerCount_Success() {
        // Given
        val memberId = 1L
        Mockito.`when`(followRepository!!.countByFollowing(user)).thenReturn(5L)

        // When
        val followerCount = followerService!!.getFollowersCount(user)

        // Then
        Assertions.assertEquals(5L, followerCount)
        Mockito.verify(followRepository).countByFollowing(user)
    }
}
