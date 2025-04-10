package com.project2.domain.member.integration.service

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.FollowService
import com.project2.domain.member.service.MemberService
import com.project2.global.dto.Empty
import com.project2.global.security.Rq
import com.project2.global.util.ImageService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(FollowServiceIntegrationTest.MockServiceConfig::class)
class FollowServiceIntegrationTest {

    @org.springframework.boot.test.context.TestConfiguration
    class MockServiceConfig {
        @Bean
        @Primary
        fun mockImageService(): ImageService = mockk {
            every { downloadProfileImage(any(), any()) } returns "/images/test-profile.png"
        }

        @Bean
        @Primary
        fun mockRq(): Rq = mockk(relaxed = true)
    }


    @Autowired
    lateinit var followService: FollowService

    @Autowired
    lateinit var followRepository: FollowRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var imageService: ImageService

    @Autowired
    lateinit var rq: Rq

    @Autowired
    lateinit var memberService: MemberService

    private fun createMemberForTest(email: String, nickname: String): Member {
        val member = memberService.signUp(email, nickname, "https://mock.com/image.png", Provider.GOOGLE)
        every { rq.getActor() } returns member
        return member
    }

    @Test
    fun `팔로우 성공 및 언팔로우 성공`() {
        // given
        val followerMember = createMemberForTest("follower@test.com", "팔로워")
        val followingMember = createMemberForTest("following@test.com", "팔로잉")

        // 여기에서 요청자 다시 지정해줘야 함!
        every { rq.getActor() } returns followerMember

        val followRequestDto = FollowRequestDto(followerMember.id!!, followingMember.id!!)

        // when (팔로우)
        val followResult = followService.toggleFollow(followRequestDto)

        // then (팔로우 성공)
        assertThat(followResult.code).isEqualTo("200")
        assertThat(followResult.msg).isEqualTo("팔로우 되었습니다.")
        assertThat(followResult.data).isNotNull
        assertThat(followRepository.findByFollowerAndFollowing(followerMember, followingMember)).isPresent

        // when (언팔로우)
        val unfollowResult = followService.toggleFollow(followRequestDto)

        // then (언팔로우 성공)
        assertThat(unfollowResult.code).isEqualTo("204")
        assertThat(unfollowResult.msg).isEqualTo("언팔로우 되었습니다.")
        assertThat(unfollowResult.data).isInstanceOf(Empty::class.java)
        assertThat(followRepository.findByFollowerAndFollowing(followerMember, followingMember)).isEmpty
    }

    @Test
    fun `본인을 팔로우 시도시 에러 응답`() {
        // given
        val member = createMemberForTest("test@gmail.com", "본인")

        val followRequestDto = FollowRequestDto(member.id!!, member.id!!)

        // when
        val result = followService.toggleFollow(followRequestDto)

        // then
        assertThat(result.code).isEqualTo("400")
        assertThat(result.msg).isEqualTo("본인을 팔로우할 수 없습니다.")
        assertThat(result.data).isInstanceOf(Empty::class.java)
        assertThat(followRepository.findAll().count()).isEqualTo(0)
    }
}
