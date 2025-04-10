package com.project2.domain.member.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.AuthTokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockCookie
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var authTokenService: AuthTokenService

    lateinit var member: Member
    lateinit var accessToken: String
    lateinit var refreshToken: String

    @BeforeEach
    fun setup() {
        member = memberRepository.save(
                Member(
                        email = "test@example.com",
                        nickname = "테스트유저",
                        provider = Provider.GOOGLE
                )
        )
        accessToken = authTokenService.genAccessToken(member)
        refreshToken = authTokenService.genRefreshToken(member.id!!)
    }

    @Test
    @DisplayName("내 정보 조회 요청 시 회원 정보를 정상적으로 반환한다")
    fun `GET 내 정보 조회`() {
        mockMvc.get("/api/members/me") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(member.id!!.toInt()) }
            jsonPath("$.data.nickname") { value(member.nickname) }
        }
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 accessToken을 재발급받는다")
    fun `GET accessToken 재발급`() {
        val refreshCookie = MockCookie("refreshToken", refreshToken)

        mockMvc.get("/api/members/refresh") {
            cookie(refreshCookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.data.nickname") { value(member.nickname) }
        }
    }

    @Test
    @DisplayName("특정 회원 프로필 조회 시 해당 회원의 정보를 반환한다")
    fun `GET 특정 회원 프로필 조회`() {
        mockMvc.get("/api/members/${member.id}") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.nickname") { value(member.nickname) }
        }
    }

    @Test
    @DisplayName("전체 회원 목록을 조회하면 회원 리스트를 반환한다")
    fun `GET 전체 회원 목록 조회`() {
        mockMvc.get("/api/members/totalMember") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data[0].id") { value(member.id!!.toInt()) }
        }
    }

    @Test
    @DisplayName("회원 닉네임을 수정하면 변경된 닉네임이 반영된다")
    fun `PUT 닉네임 수정`() {
        val newNickname = "변경된유저"
        val payload = mapOf("nickname" to newNickname)

        mockMvc.put("/api/members/nickname/${member.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = ObjectMapper().writeValueAsString(payload)
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.nickname") { value(newNickname) }
        }
    }

    @Test
    @DisplayName("회원 프로필 이미지를 수정하면 정상적으로 저장된다")
    fun `PUT 프로필 이미지 수정`() {
        val image = MockMultipartFile(
                "profileImage",
                "image.jpg",
                "image/jpeg",
                "fake-image-content".toByteArray()
        )

        val requestBuilder = multipart("/api/members/profile-image/${member.id}")
                .file(image)
                .header("Authorization", "Bearer $accessToken")
                .with { request -> request.method = "PUT"; request }

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("200"))
    }

    @Test
    @DisplayName("로그아웃 요청 시 성공 메시지를 반환한다")
    fun `DELETE 로그아웃`() {
        mockMvc.delete("/api/members/logout") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.msg") { value("로그아웃이 완료되었습니다.") }
        }
    }
}