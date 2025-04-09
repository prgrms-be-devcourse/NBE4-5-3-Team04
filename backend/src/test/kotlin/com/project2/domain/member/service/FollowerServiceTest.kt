package com.project2.domain.member.service

import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class FollowerServiceTest {

    @MockK
    lateinit var followRepository: FollowRepository

    @MockK
    lateinit var memberService: MemberService

    @MockK
    lateinit var rq: Rq

    @InjectMockKs
    lateinit var followerService: FollowerService

    private lateinit var user: Member
    private lateinit var follower1: Member
    private lateinit var follower2: Member

    @BeforeEach
    fun setUp() {
        user = Member().apply { id = 1L; nickname = "testUser" }
        follower1 = Member().apply { id = 2L; nickname = "follower1" }
        follower2 = Member().apply { id = 3L; nickname = "follower2" }
    }

    @Test
    fun `getFollowers - pass case`() {
        every { rq.getActor() } returns user
        every { memberService.findByIdOrThrow(1L) } returns user

        val follows = listOf(
                Follows(null, follower1, user),
                Follows(null, follower2, user)
        )
        every { followRepository.findByFollowing(eq(user), any()) } returns PageImpl(follows)

        val result = followerService.getFollowers(1L, Pageable.unpaged())

        assertEquals(2, result.content.size)
        assertTrue(result.content.any { it.userId == follower1.id })
        assertTrue(result.content.any { it.userId == follower2.id })

        verify { rq.getActor() }
        verify { memberService.findByIdOrThrow(1L) }
        verify { followRepository.findByFollowing(eq(user), any()) }
    }

    @Test
    fun `getFollowers - no follower`() {
        every { rq.getActor() } returns user
        every { memberService.findByIdOrThrow(1L) } returns user
        every { followRepository.findByFollowing(eq(user), any()) } returns Page.empty()

        val result = followerService.getFollowers(1L, Pageable.unpaged())

        assertTrue(result.isEmpty)
    }

    @Test
    fun `getFollowers - throw exception`() {
        every { rq.getActor() } returns user
        every { memberService.findByIdOrThrow(1L) } throws ServiceException("404", "사용자를 찾을 수 없습니다.")

        val exception = assertFailsWith<ServiceException> {
            followerService.getFollowers(1L, Pageable.unpaged())
        }

        assertEquals("404", exception.code)
    }

    @Test
    fun `getFollowersCount - pass case`() {
        every { followRepository.countByFollowing(user) } returns 5L

        val count = followerService.getFollowersCount(user)

        assertEquals(5L, count)
    }
}
