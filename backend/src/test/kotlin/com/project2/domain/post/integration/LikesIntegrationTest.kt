package com.project2.domain.post.integration

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import com.project2.global.security.SecurityUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class LikesIntegrationTest @Autowired constructor(
    val mockMvc: MockMvc,
    val memberRepository: MemberRepository,
    val postRepository: PostRepository,
) {

    private lateinit var member: Member
    private lateinit var post: Post

    @BeforeEach
    fun setUp() {
        member = memberRepository.save(Member(email = "test@example.com", nickname = "nickname"))

        val postTemp = Post().apply {
            title = "Sample"
            content = "Content"
        }

        postTemp.member = member
        post = postRepository.save(postTemp)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            SecurityUser(member),
            "",
            SecurityUser(member).authorities
        )
    }

    @Test
    @DisplayName("POST /api/posts/{id}/like - 좋아요 추가 및 응답 확인")
    fun t1() {
        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("좋아요 상태 변경 완료") }
            jsonPath("$.data.liked") { value(true) }
        }
    }

    @Test
    @DisplayName("POST /api/posts/{id}/like - 좋아요 삭제 및 응답 확인")
    fun t2() {
        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
        }

        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("좋아요 상태 변경 완료") }
            jsonPath("$.data.liked") { value(false) }
        }
    }

    @Test
    @DisplayName("좋아요 수 확인")
    fun t3() {
        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.likeCount") { value(1) }
        }

        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.likeCount") { value(0) }
        }
    }

//    TODO: 존재하지 않는 게시물에 대한 예외 응답 처리 필요
//    @Test
//    @DisplayName("존재하지 않는 게시물(삭제된 게시물)에 좋아요 요청 시 404 반환")
//    fun t4() {
//        val nonExistentPostId = 99999L
//
//        mockMvc.post("/api/posts/$nonExistentPostId/likes") {
//            contentType = MediaType.APPLICATION_JSON
//            header("user", member.id!!)
//        }.andExpect {
//            status { isNotFound() }
//            jsonPath("$.code") { value("404") }
//            jsonPath("$.msg") { value("게시물을 찾을 수 없습니다.") }
//        }
//    }

    @Test
    @DisplayName("사용자별 좋아요 상태가 분리되는지 검증")
    fun t5() {
        val anotherMember = memberRepository.save(Member(email = "test2@example.com", nickname = "user2"))

        // member가 먼저 좋아요 클릭
        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.liked") { value(true) }
        }

        // 다른 사용자인 user2로 요청 시 liked == false여야 함
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            SecurityUser(anotherMember),
            "",
            SecurityUser(anotherMember).authorities
        )

        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", anotherMember.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.liked") { value(true) } // 첫 요청이기 때문에 이 요청은 좋아요 추가
        }

        // 다시 요청하면 해제됨
        mockMvc.post("/api/posts/${post.id}/likes") {
            contentType = MediaType.APPLICATION_JSON
            header("user", anotherMember.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.liked") { value(false) }
        }
    }
}