package com.project2.domain.post.integration.controller

import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.AuthTokenService
import com.project2.domain.place.entity.Place
import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.place.repository.PlaceRepository
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var placeRepository: PlaceRepository

    @Autowired
    lateinit var postRepository: PostRepository

    @Autowired
    lateinit var authTokenService: AuthTokenService

    lateinit var member: Member
    lateinit var accessToken: String

    @BeforeEach
    fun setup() {
        member = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "통합테스트유저",
                provider = Provider.GOOGLE
            )
        )

        accessToken = authTokenService.genAccessToken(member)

        val place = placeRepository.save(
            Place(
                id = 1L,
                name = "테스트장소",
                latitude = 37.5,
                longitude = 127.0,
                region = Region.SEOUL,
                category = Category.CE7
            )
        )

        postRepository.save(
            Post.builder()
                .title("테스트 게시글")
                .content("내용입니다.")
                .member(member)
                .place(place)
                .build()
        )
    }

    @Test
    fun `GET 게시글 전체 조회`() {
        mockMvc.get("/api/posts") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.content[0].title") { value("테스트 게시글") }
        }
    }

    @Test
    fun `GET 게시글 단건 조회`() {
        val postId = postRepository.findAll().first().id!!

        mockMvc.get("/api/posts/$postId") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.title") { value("테스트 게시글") }
        }
    }

    @Test
    fun `GET 게시글 수정용 단건 조회`() {
        val postId = postRepository.findAll().first().id!!

        mockMvc.get("/api/posts/$postId/for-edit") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.title") { value("테스트 게시글") }
        }
    }

    @Test
    fun `DELETE 게시글 삭제`() {
        val postId = postRepository.findAll().first().id!!

        mockMvc.delete("/api/posts/$postId") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `PUT 게시글 수정`() {
        val postId = postRepository.findAll().first().id!!

        mockMvc.put("/api/posts/$postId") {
            header("Authorization", "Bearer $accessToken")
            param("title", "수정된 제목")
            param("content", "수정된 내용")
            param("placeId", "1")
            param("placeName", "테스트장소")
            param("latitude", "37.5")
            param("longitude", "127.0")
            param("region", "SEOUL")
            param("category", "CE7")
            param("memberId", member.id!!.toString())
            contentType = MediaType.MULTIPART_FORM_DATA
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST 게시글 생성`() {
        mockMvc.post("/api/posts") {
            header("Authorization", "Bearer $accessToken")
            param("title", "새 게시글")
            param("content", "새로운 내용")
            param("placeId", "1")
            param("placeName", "테스트장소")
            param("latitude", "37.5")
            param("longitude", "127.0")
            param("region", "SEOUL")
            param("category", "CE7")
            param("memberId", member.id!!.toString())
            contentType = MediaType.MULTIPART_FORM_DATA
        }.andExpect {
            status { isCreated() }
        }
    }
}