package com.project2.domain.member.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.AuthTokenService
import com.project2.global.util.ImageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(FollowControllerIntegrationTest.MockConfig::class)
class FollowControllerIntegrationTest {

    @TestConfiguration
    class MockConfig {
        @Bean
        fun imageService(): ImageService = Mockito.mock(ImageService::class.java)
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var followRepository: FollowRepository

    @Autowired
    lateinit var authTokenService: AuthTokenService

    @Autowired
    lateinit var imageService: ImageService

    lateinit var follower: Member
    lateinit var following: Member
    lateinit var accessToken: String

    val objectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        whenever(
            imageService.downloadProfileImage(
                eq("https://mock.com/image.png"),
                any<Long>()
            )
        ).thenReturn("/images/test-profile.png")

        follower = memberRepository.save(
            Member().apply {
                email = "test1@test.com"
                nickname = "test1"
                provider = Provider.NAVER
                createdDate = LocalDateTime.now()
            })

        following = memberRepository.save(
            Member().apply {
                email = "test2@test.com"
                nickname = "test2"
                provider = Provider.NAVER
                createdDate = LocalDateTime.now()
            })

        accessToken = authTokenService.genAccessToken(follower)
    }

    @Test
    fun `팔로우 및 언팔로우 성공`() {
        val requestDto = FollowRequestDto(follower.id!!, following.id!!)
        val payload = objectMapper.writeValueAsString(requestDto)

        // 팔로우 요청
        mockMvc.post("/api/follows/${follower.id}/follows") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
        }

        // 언팔로우 요청
        mockMvc.post("/api/follows/${follower.id}/follows") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isNoContent() }
            jsonPath("$.code") { value("204") }
        }
    }

    @Test
    fun `본인 팔로우 시 400 에러`() {
        val self = memberRepository.save(
            Member().apply {
                email = "test@test.com"
                nickname = "test"
                provider = Provider.NAVER
                createdDate = LocalDateTime.now()
            })
        val token = authTokenService.genAccessToken(self)

        val requestDto = FollowRequestDto(self.id!!, self.id!!)
        val payload = objectMapper.writeValueAsString(requestDto)

        mockMvc.post("/api/follows/${self.id}/follows") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("400") }
            jsonPath("$.msg") { value("본인을 팔로우할 수 없습니다.") }
        }
    }
}
