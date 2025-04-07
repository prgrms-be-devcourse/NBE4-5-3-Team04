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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class FollowingServiceTest {
    @Mock
    private val followRepository: FollowRepository? = null

    @Mock
    private val memberService: MemberService? = null

    @Mock
    private val rq: Rq? = null

    @InjectMocks
    private val followingService: FollowingService? = null

    private var user: Member? = null
    private var following1: Member? = null
    private var following2: Member? = null

    @BeforeEach
    fun setUp() {
        // Create test members
        user = Member()
        user!!.id = 1L
        user!!.nickname = "testUser"

        following1 = Member()
        following1!!.id = 2L
        following1!!.nickname = "follower1"

        following2 = Member()
        following2!!.id = 3L
        following2!!.nickname = "follower2"
    }

    @Test
    fun testGetFollowings_Success() {
        // Given
        Mockito.`when`(rq!!.actor).thenReturn(user)
        Mockito.`when`(memberService!!.findByIdOrThrow(1L)).thenReturn(user)

        val follow1 = Follows(null,user,following1)
        follow1.follower = user
        follow1.following = following1

        val follow2 = Follows(null,user,following2)
        follow2.follower = user
        follow2.following = following2

        Mockito.`when`(followRepository!!.findByFollower(user)).thenReturn(Arrays.asList(follow1, follow2))

        // When
        val followings = followingService!!.getFollowings(1L)

        // Then
        Assertions.assertNotNull(followings)
        Assertions.assertEquals(2, followings.size)

        Assertions.assertTrue(followings.stream().anyMatch { f: FollowerResponseDto ->
            f.id!!.equals(
                following1!!.id
            )
        })
        Assertions.assertTrue(followings.stream().anyMatch { f: FollowerResponseDto ->
            f.id!!.equals(
                following2!!.id
            )
        })

        Mockito.verify(memberService).findByIdOrThrow(1L)
        Mockito.verify(followRepository).findByFollower(user)
    }

    @Test
    fun testGetFollowings_NoFollowings() {
        // Given
        Mockito.`when`(rq!!.actor).thenReturn(user)
        Mockito.`when`(memberService!!.findByIdOrThrow(1L)).thenReturn(user)
        Mockito.`when`(followRepository!!.findByFollower(user)).thenReturn(mutableListOf())

        // When
        val followings = followingService!!.getFollowings(1L)

        // Then
        Assertions.assertNotNull(followings)
        Assertions.assertTrue(followings.isEmpty())
    }

    @Test
    fun testGetFollowings_UserNotFound() {
        // Given
        Mockito.`when`(rq!!.actor).thenReturn(user)
        Mockito.`when`(memberService!!.findByIdOrThrow(1L)).thenThrow(ServiceException("404", "사용자를 찾을 수 없습니다."))

        // When & Then
        val exception = Assertions.assertThrows(
            ServiceException::class.java
        ) {
            followingService!!.getFollowings(1L)
        }

        // Then
        Assertions.assertEquals("404", exception.code)
        Assertions.assertEquals("사용자를 찾을 수 없습니다.", exception.message)

        // Verify interactions
        Mockito.verify(rq).actor
        Mockito.verify(memberService).findByIdOrThrow(1L)
    }

    @Test
    @DisplayName("회원이 존재할 경우 팔로잉 수를 정상적으로 반환해야 한다")
    fun testGetFollowingsCount_Success() {
        // Given
        Mockito.`when`(followRepository!!.countByFollower(user)).thenReturn(5L)

        // When
        val followingCount = followingService!!.getFollowingsCount(user)

        // Then
        Assertions.assertEquals(5L, followingCount)
        Mockito.verify(followRepository).countByFollower(user)
    }
}
