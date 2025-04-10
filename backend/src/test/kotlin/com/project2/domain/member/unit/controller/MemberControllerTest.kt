package com.project2.domain.member.unit.controller

import com.project2.domain.member.controller.MemberController
import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.service.*
import com.project2.global.security.Rq
import com.project2.global.security.SecurityUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class MemberControllerTest {

    private lateinit var memberController: MemberController

    @Mock
    private lateinit var authService: AuthService

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var rq: Rq

    private lateinit var mockMvc: MockMvc
    private lateinit var mockMember: Member
    private lateinit var actor: SecurityUser

    @BeforeEach
    fun setUp() {
        // 직접 객체 생성
        memberController = MemberController(
                authService = authService,
                memberService = memberService,
                postService = mock(),       // 사용하지 않음
                followerService = mock(),   // 사용하지 않음
                followingService = mock(),  // 사용하지 않음
                rq = rq
        )

        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build()

        mockMember = Member().apply {
            id = 1L
            email = "test@test.com"
            nickname = "test"
            provider = Provider.NAVER
            createdDate = LocalDateTime.now()
        }

        actor = SecurityUser(mockMember)

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(actor, null, actor.authorities)
        SecurityContextHolder.setContext(context)
    }

    @Test
    @DisplayName("사용자 정보 조회 - 성공적으로 응답을 반환한다.")
    fun `getAuthenticatedMemberInfo - success`() {
        // given
        `when`(rq.getActor()).thenReturn(mockMember)
        `when`(rq.getRealActor(mockMember)).thenReturn(mockMember)

        // when & then
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(mockMember.id))
                .andExpect(jsonPath("$.data.nickname").value(mockMember.nickname))
                .andExpect(jsonPath("$.data.profileImageUrl").value(mockMember.getProfileImageUrlOrDefaultUrl()))
                .andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."))
    }

    @Test
    @DisplayName("리프레시 토큰 없이 요청 시 401 반환")
    fun `refreshAccessToken - no token - returns 401`() {
        // given
        `when`(rq.getValueFromCookie("refreshToken")).thenReturn(null)

        // when & then
        mockMvc.perform(get("/api/members/refresh"))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.msg").value("리프레시 토큰이 제공되지 않았습니다."))
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 갱신 성공")
    fun `refreshAccessToken - valid token - returns new accessToken`() {
        // given
        val validRefreshToken = "valid-refresh-token"
        val newAccessToken = "new-access-token"

        `when`(rq.getValueFromCookie("refreshToken")).thenReturn(validRefreshToken)
        `when`(authService.getMemberByRefreshTokenOrThrow(validRefreshToken)).thenReturn(mockMember)
        `when`(rq.getRealActor(mockMember)).thenReturn(mockMember)
        `when`(authService.genAccessToken(mockMember)).thenReturn(newAccessToken)

        // when & then
        mockMvc.perform(get("/api/members/refresh"))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("액세스 토큰이 갱신되었습니다."))
    }
}
