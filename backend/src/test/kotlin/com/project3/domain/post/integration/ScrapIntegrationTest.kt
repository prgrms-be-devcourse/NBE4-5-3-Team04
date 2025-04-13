package com.project3.domain.post.integration

import com.project3.domain.member.entity.Member
import com.project3.domain.member.repository.MemberRepository
import com.project3.domain.post.entity.Post
import com.project3.domain.post.repository.PostRepository
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
import com.project3.global.security.SecurityUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ScrapIntegrationTest @Autowired constructor(
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
    @DisplayName("POST /api/posts/{id}/scrap - 스크랩 추가 및 응답 확인")
    fun t1 () {
        mockMvc.post("/api/posts/${post.id}/scrap") {
            contentType = MediaType.APPLICATION_JSON

            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("스크랩 상태 변경 완료") }
            jsonPath("$.data.scrapped") { value(true) }
        }
    }

    @Test
    @DisplayName("POST /api/posts/{id}/scrap - 스크랩 삭제 및 응답 확인")
    fun t2 () {
        // 스크랩을 수행
        mockMvc.post("/api/posts/${post.id}/scrap") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
        }

        // 다시 요청해서 삭제
        mockMvc.post("/api/posts/${post.id}/scrap") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("스크랩 상태 변경 완료") }
            jsonPath("$.data.scrapped") { value(false) }
        }
    }

    @Test
    @DisplayName("스크랩 수 확인")
    fun t3 () {
        mockMvc.post("/api/posts/${post.id}/scrap") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.scrapCount") { value(1) }
        }

        mockMvc.post("/api/posts/${post.id}/scrap") {
            contentType = MediaType.APPLICATION_JSON
            header("user", member.id!!)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.scrapCount") { value(0) }
        }
    }
}