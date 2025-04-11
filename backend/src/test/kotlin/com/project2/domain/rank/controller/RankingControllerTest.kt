package com.project2.domain.rank.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.project2.domain.place.enums.Region
import com.project2.domain.post.dto.PostResponseDTO
import com.project2.domain.rank.dto.PopularPlaceResponseDTO
import com.project2.domain.rank.dto.RegionRankingResponseDTO
import com.project2.domain.rank.enums.RankingPeriod
import com.project2.domain.rank.service.RankingService
import com.project2.global.security.SecurityUser
import com.project2.domain.place.entity.Place
import com.project2.domain.member.entity.Member
import com.project2.domain.post.entity.Post
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.security.Principal

class RankingControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var rankingService: RankingService
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        rankingService = mockk()
        val controller = RankingController(rankingService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setCustomArgumentResolvers(
            PageableHandlerMethodArgumentResolver()
        ).build()
    }

    @Test
    @DisplayName("인기 장소 목록 조회 API 테스트")
    fun `getPopularPlaces - 성공적으로 응답을 반환한다`() {
        // given
        val period = RankingPeriod.ONE_MONTH
        val placeName = "인기장소"
        val page = 0
        val size = 10
        val pageable = PageRequest.of(page, size)
        val mockResponse = listOf(
            PopularPlaceResponseDTO(1L, "인기 장소 1", Region.SEOUL.name, 10, 5, 20),
            PopularPlaceResponseDTO(2L, "인기 장소 2", Region.BUSAN.name, 8, 3, 15)
        )
        val popularPlacesPage = PageImpl(mockResponse, pageable, mockResponse.size.toLong())

        every { rankingService.getPopularPlaces(period, placeName, pageable) } returns popularPlacesPage

        // when & then
        mockMvc.perform(
            get("/api/rankings/places")
                .param("period", period.name)
                .param("placeName", placeName)
                .param("page", page.toString())
                .param("size", size.toString())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.data.content[0].placeId").value(1))
            .andExpect(jsonPath("$.data.content[0].placeName").value("인기 장소 1"))
            .andExpect(jsonPath("$.data.content[1].placeId").value(2))
            .andExpect(jsonPath("$.data.content[1].placeName").value("인기 장소 2"))
            .andExpect(jsonPath("$.data.totalElements").value(mockResponse.size))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.msg").value("인기 장소 조회 성공"))

        verify(exactly = 1) { rankingService.getPopularPlaces(period, placeName, pageable) }
    }

    @Test
    @DisplayName("지역별 랭킹 조회 API 테스트")
    fun `getRegionRankings - 성공적으로 응답을 반환한다`() {
        // given
        val period = RankingPeriod.ONE_MONTH
        val page = 0
        val size = 5
        val pageable = PageRequest.of(page, size)
        val mockResponse = listOf(
            RegionRankingResponseDTO(Region.SEOUL.name, Region.SEOUL, 15, 50, 1),
            RegionRankingResponseDTO(Region.BUSAN.name, Region.BUSAN, 10, 40, 1)
        )
        val regionRankingsPage = PageImpl(mockResponse, pageable, mockResponse.size.toLong())

        every { rankingService.getRegionRankings(period, pageable) } returns regionRankingsPage

        // when & then
        mockMvc.perform(
            get("/api/rankings/regions")
                .param("period", period.name)
                .param("page", page.toString())
                .param("size", size.toString())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.data.content[0].region").value(Region.SEOUL.name))
            .andExpect(jsonPath("$.data.content[0].postCount").value(1))
            .andExpect(jsonPath("$.data.content[1].region").value(Region.BUSAN.name))
            .andExpect(jsonPath("$.data.content[1].postCount").value(1))
            .andExpect(jsonPath("$.data.totalElements").value(mockResponse.size))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.msg").value("인기 지역 랭킹 조회 성공"))

        verify(exactly = 1) { rankingService.getRegionRankings(period, pageable) }
    }

}