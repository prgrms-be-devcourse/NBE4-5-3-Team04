package com.project3.domain.rank.integration.service

import com.project3.domain.member.entity.Member
import com.project3.domain.member.enums.Provider
import com.project3.domain.member.repository.MemberRepository
import com.project3.domain.place.entity.Place
import com.project3.domain.place.enums.Category
import com.project3.domain.place.enums.Region
import com.project3.domain.place.repository.PlaceRepository
import com.project3.domain.post.entity.Post
import com.project3.domain.post.repository.PostRepository
import com.project3.domain.rank.enums.RankingPeriod
import com.project3.domain.rank.service.RankingService
import com.project3.global.security.SecurityUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RankingServiceIntegrationTest {

    @Autowired
    private lateinit var rankingService: RankingService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    private lateinit var actor: SecurityUser
    private lateinit var place: Place
    private lateinit var region: Region

    @BeforeEach
    fun setup() {
        val member = memberRepository.save(
                Member(
                        email = "user@example.com",
                        nickname = "유저1",
                        profileImageUrl = null,
                        provider = Provider.NAVER,
                )
        )
        actor = SecurityUser(member)

        place = placeRepository.saveAndFlush(
                Place(
                        id = 1L,
                        name = "테스트장소",
                        latitude = 0.0,
                        longitude = 0.0,
                        region = Region.SEOUL,
                        category = Category.fromKrCategory("기타")
                )
        )
        region = Region.SEOUL

        repeat(5) {
            val post = Post().apply {
                title = "테스트 제목 $it"
                content = "테스트 게시글 $it"
                this.member = member
            }
            post.place = place
            postRepository.save(post)
        }
    }

    @Test
    @DisplayName("전국 인기 장소 조회 통합 테스트")
    fun getPopularPlacesTest() {
        val result = rankingService.getPopularPlaces(RankingPeriod.ONE_MONTH, null, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("테스트장소", result.content[0].placeName)
    }

    @Test
    @DisplayName("인기 지역 랭킹 조회 통합 테스트")
    fun getRegionRankingsTest() {
        val result = rankingService.getRegionRankings(RankingPeriod.ONE_MONTH, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals(region, result.content[0].region)
    }

    @Test
    @DisplayName("특정 장소 게시글 조회 통합 테스트")
    fun getPostsByPlaceTest() {
        val result = rankingService.getPostsByPlace(place.id, RankingPeriod.ONE_MONTH, PageRequest.of(0, 10), actor)

        assertEquals(5, result.totalElements)
        assertEquals("테스트 게시글 0", result.content[0].content)
    }

    @Test
    @DisplayName("특정 지역 게시글 조회 통합 테스트")
    fun getPostsByRegionTest() {
        val result = rankingService.getPostsByRegion(region, RankingPeriod.ONE_MONTH, PageRequest.of(0, 10), actor)

        assertEquals(5, result.totalElements)
        assertEquals("테스트 게시글 0", result.content[0].content)
    }
}
