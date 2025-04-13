package com.project3.domain.rank.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.project3.domain.member.entity.Member
import com.project3.domain.member.repository.MemberRepository
import com.project3.domain.member.service.AuthTokenService
import com.project3.domain.rank.enums.RankingPeriod
import com.project3.domain.rank.service.RankingService
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RankingControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var rankingService: RankingService

    @Autowired
    lateinit var authTokenService: AuthTokenService

    @Autowired
    lateinit var memberRepository: MemberRepository

    private val objectMapper = ObjectMapper()

    private lateinit var accessToken: String

    @BeforeEach
    fun setup() {
        val member = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "testUser"
            )
        )
        accessToken = authTokenService.genAccessToken(member)
    }

    @Test
    fun `인기 장소 랭킹 조회 - 통합 테스트`() {
        val period = RankingPeriod.ONE_MONTH
        val placeName = "장소"
        val page = 0
        val size = 10

        mockMvc.get("/api/rankings/places") {
            param("period", period.name)
            param("placeName", placeName)
            param("page", page.toString())
            param("size", size.toString())
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("인기 장소 조회 성공") }
            jsonPath("$.data", notNullValue())
        }
    }

    @Test
    fun `지역별 랭킹 조회 - 통합 테스트`() {
        val period = RankingPeriod.ONE_MONTH
        val page = 0
        val size = 5

        mockMvc.get("/api/rankings/regions") {
            param("period", period.name)
            param("page", page.toString())
            param("size", size.toString())
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("인기 지역 랭킹 조회 성공") }
            jsonPath("$.data", notNullValue())
        }
    }
}
