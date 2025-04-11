package com.project2.domain.post.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.project2.domain.post.dto.comment.CommentRequestDTO
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

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
    var postId: Long = 0L
    var commentId: Long = 0L

    @BeforeEach
    fun setup() {
        member = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "댓글통합유저",
                provider = Provider.GOOGLE
            )
        )
        accessToken = authTokenService.genAccessToken(member)

        val place = placeRepository.save(
            Place(
                id = 1L,
                name = "장소",
                latitude = 37.5,
                longitude = 127.0,
                region = Region.SEOUL,
                category = Category.CE7
            )
        )

        val post = postRepository.save(
            Post.builder()
                .title("댓글 테스트 게시글")
                .content("댓글 내용")
                .member(member)
                .place(place)
                .build()
        )
        postId = post.id!!
    }

    @Test
    @DisplayName("댓글 생성")
    fun `POST 댓글 생성`() {
        val request = CommentRequestDTO(content = "댓글 내용", parentId = null)

        mockMvc.post("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @DisplayName("댓글 조회")
    fun `GET 댓글 조회`() {
        val create = CommentRequestDTO(content = "조회용 댓글", parentId = null)

        mockMvc.post("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(create)
        }.andExpect {
            status { isOk() }
        }

        mockMvc.get("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @DisplayName("댓글 수정")
    fun `PUT 댓글 수정`() {
        val create = CommentRequestDTO(content = "수정 전 댓글", parentId = null)

        val mvcResult = mockMvc.post("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(create)
        }.andReturn()

        val json = mvcResult.response.contentAsString
        commentId = objectMapper.readTree(json).path("data").path("id").asLong()

        val update = CommentRequestDTO(content = "수정된 댓글", parentId = null)

        mockMvc.put("/api/comments/$commentId") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(update)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    fun `DELETE 댓글 삭제`() {
        val create = CommentRequestDTO(content = "삭제용 댓글", parentId = null)

        val mvcResult = mockMvc.post("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(create)
        }.andReturn()

        val json = mvcResult.response.contentAsString
        commentId = objectMapper.readTree(json).path("data").path("id").asLong()

        mockMvc.delete("/api/comments/$commentId") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
        }
    }
}
