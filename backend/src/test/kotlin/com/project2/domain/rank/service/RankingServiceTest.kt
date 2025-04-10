package com.project2.domain.rank.service

import com.project2.domain.member.entity.Member
import com.project2.domain.place.entity.Place
import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.post.entity.Post
import com.project2.domain.post.dto.PostResponseDTO
import com.project2.domain.post.entity.Likes
import com.project2.domain.rank.dto.PopularPlaceDTO
import com.project2.domain.rank.dto.PopularPlaceResponseDTO
import com.project2.domain.rank.dto.RegionRankingDTO
import com.project2.domain.rank.dto.RegionRankingResponseDTO
import com.project2.domain.rank.enums.RankingPeriod
import com.project2.domain.rank.repository.RankingRepository
import com.project2.global.security.SecurityUser
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class RankingServiceTest {

    @MockK
    private lateinit var rankingRepository: RankingRepository

    @InjectMockKs
    private lateinit var rankingService: RankingService

    private lateinit var mockPlace1: Place
    private lateinit var mockPlace2: Place
    private lateinit var mockPost1: Post
    private lateinit var mockPost2: Post
    private lateinit var mockMember: Member
    private lateinit var mockSecurityUser: SecurityUser
    private lateinit var startDate: LocalDateTime

    @BeforeEach
    fun setUp() {
        mockPlace1 = Place().apply {
            id = 1L
            name = "인기 장소 1"
            latitude = 0.0
            longitude = 0.0
            region = Region.SEOUL
            category = Category.ETC
            posts = mutableListOf()
        }
        mockPlace2 = Place().apply {
            id = 2L
            name = "다른 장소"
            latitude = 0.0
            longitude = 0.0
            region = Region.BUSAN
            category = Category.ETC
            posts = mutableListOf()
        }
        mockMember = Member().apply { id = 1L }
        mockPost1 = Post().apply {
            id = 10L
            title = "게시글 1"
            content = "내용 1"
            member = mockMember
            place = mockPlace1
            createdDate = LocalDateTime.now().minusMonths(3)
            likes = mutableSetOf(mockk<Likes> { every { member } returns mockMember }) // member를 가진 Likes Mock
        }
        mockPost2 = Post().apply {
            id = 11L
            title = "게시글 2"
            content = "내용 2"
            member = mockMember
            place = mockPlace2
            createdDate = LocalDateTime.now().minusMonths(3)
            likes = mutableSetOf(
                mockk<Likes> { every { member } returns mockMember },
                mockk<Likes> { every { member } returns mockk() }) // member들을 가진 Likes Mock
        }
        mockSecurityUser = SecurityUser(mockMember.id!!, "test@test.com", listOf())
        startDate = RankingPeriod.ONE_MONTH.getStartDate()
    }

    @Test
    fun `getPopularPlaces - 인기 장소 목록 조회 성공`() {
        // given
        val period = RankingPeriod.SIX_MONTHS
        val placeName = "인기장소"
        val pageable = PageRequest.of(0, 5)
        val startDate = period.getStartDate() // startDate 초기화
        val mockRankingResult = listOf(
            PopularPlaceDTO(mockPlace1.id, mockPlace1.name, mockPlace1.region, 1, 0, 1),
            PopularPlaceDTO(mockPlace2.id, mockPlace2.name, mockPlace2.region, 0, 1, 1)
        )
        val expectedResponse = mockRankingResult.map { it.toResponse() }
        val popularPlacesPage = PageImpl(mockRankingResult, pageable, mockRankingResult.size.toLong())

        every { rankingRepository.findPopularPlaces(any(), placeName, pageable) } returns popularPlacesPage

        // when
        val resultPage = rankingService.getPopularPlaces(period, placeName, pageable)

        // then
        assertNotNull(resultPage)
        assertEquals(expectedResponse.size, resultPage.content.size)
        assertEquals(expectedResponse[0].placeId, resultPage.content[0].placeId)
        assertEquals(expectedResponse[0].placeName, resultPage.content[0].placeName)
        assertEquals(expectedResponse[0].postCount, resultPage.content[0].postCount)
        assertEquals(expectedResponse[0].region, resultPage.content[0].region)
    }

    @Test
    fun `getRegionRankings - 인기 지역 랭킹 조회 성공`() {
        // given
        val period = RankingPeriod.ONE_MONTH
        val pageable = PageRequest.of(0, 10)
        val mockRankingResult = listOf(
            RegionRankingDTO(Region.SEOUL, 2, 0, 1), RegionRankingDTO(Region.BUSAN, 1, 1, 1)
        )
        val expectedResponse = mockRankingResult.map { it.toResponse() }
        val regionRankingsPage = PageImpl(mockRankingResult, pageable, mockRankingResult.size.toLong())

        every { rankingRepository.findRegionRankings(any(), pageable) } returns regionRankingsPage

        // when
        val resultPage = rankingService.getRegionRankings(period, pageable)

        // then
        assertNotNull(resultPage)
        assertEquals(expectedResponse.size, resultPage.content.size)
        assertEquals(expectedResponse[0].region, resultPage.content[0].region)
        assertEquals(expectedResponse[0].postCount, resultPage.content[0].postCount)
    }

    @Test
    fun `getPostsByRegion - 특정 지역의 게시글 목록 조회 성공`() {
        // given
        val region = Region.SEOUL
        val period = RankingPeriod.ONE_MONTH
        val pageable = PageRequest.of(0, 5)
        val mockPosts = listOf(mockPost1)
        val expectedResponse = mockPosts.map { PostResponseDTO(it, mockSecurityUser) }
        val postsPage = PageImpl(mockPosts, pageable, mockPosts.size.toLong())

        every { rankingRepository.findPostsByRegion(region, any(), pageable) } returns postsPage

        // when
        val resultPage = rankingService.getPostsByRegion(region, period, pageable, mockSecurityUser)

        // then
        assertNotNull(resultPage)
        assertEquals(expectedResponse.size, resultPage.content.size)
        val actualPost = resultPage.content[0]
        val expectedPost = expectedResponse[0]
        assertEquals(expectedPost.id, actualPost.id)
        assertEquals(expectedPost.title, actualPost.title)
        assertEquals(expectedPost.placeDTO.placeName, actualPost.placeDTO.placeName)
        assertEquals(expectedPost.placeDTO.category, actualPost.placeDTO.category)
        assertEquals(expectedPost.likeCount, actualPost.likeCount)
        assertEquals(expectedPost.isLiked, actualPost.isLiked)
        assertEquals(expectedPost.scrapCount, actualPost.scrapCount)
        assertEquals(expectedPost.isScrapped, actualPost.isScrapped)
        assertEquals(expectedPost.commentCount, actualPost.commentCount)
        assertEquals(expectedPost.imageUrls, actualPost.imageUrls)
        assertEquals(expectedPost.author.memberId, actualPost.author.memberId)
        assertEquals(expectedPost.author.nickname, actualPost.author.nickname)
    }

    @Test
    fun `getPostsByPlace - 특정 장소의 게시글 목록 조회 성공`() {
        // given
        val placeId = 1L
        val period = RankingPeriod.ONE_MONTH
        val pageable = PageRequest.of(0, 3)
        val mockPosts = listOf(mockPost1)
        val expectedResponse = mockPosts.map { PostResponseDTO(it, mockSecurityUser) }
        val postsPage = PageImpl(mockPosts, pageable, mockPosts.size.toLong())

        every { rankingRepository.findPostsByPlace(placeId, any(), pageable) } returns postsPage

        // when
        val resultPage = rankingService.getPostsByPlace(placeId, period, pageable, mockSecurityUser)

        // then
        assertNotNull(resultPage)
        assertEquals(expectedResponse.size, resultPage.content.size)
        assertEquals(expectedResponse[0].id, resultPage.content[0].id)
        assertEquals(expectedResponse[0].title, resultPage.content[0].title)
        assertEquals(expectedResponse[0].placeDTO.placeName, resultPage.content[0].placeDTO.placeName)
    }
}
