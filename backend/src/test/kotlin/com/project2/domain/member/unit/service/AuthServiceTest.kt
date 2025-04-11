package com.project2.domain.member.unit.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.AuthService
import com.project2.domain.member.service.AuthTokenService
import com.project2.global.exception.ServiceException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.reflect.Field
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    private val validAccessToken = "validAccessToken"
    private val invalidAccessToken = "invalidAccessToken"
    private val validRefreshToken = "validRefreshToken"
    private val invalidRefreshToken = "invalidRefreshToken"

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var authTokenService: AuthTokenService

    @InjectMocks
    private lateinit var authService: AuthService

    private lateinit var mockMember: Member

    @BeforeEach
    fun setUp() {
        injectValue(authTokenService, "keyString", "test-secret-key")
        injectValue(authTokenService, "accessTokenExpireSeconds", 3600)
        injectValue(authTokenService, "refreshTokenExpireSeconds", 7200)

        mockMember = Member().apply {
            id = 1L
            email = "test@example.com"
        }
    }

    private fun injectValue(target: Any, fieldName: String, value: Any) {
        val field: Field = target::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    @Test
    @DisplayName("정상적인 Access Token을 사용하여 회원 정보를 조회할 수 있다.")
    fun `getMemberByAccessToken should return member when token is valid`() {
        val payload: Map<String, Any> = mapOf("id" to mockMember.id!!, "email" to mockMember.email)
        `when`(authTokenService.getPayload(validAccessToken)).thenReturn(payload)

        val result = authService.getMemberByAccessToken(validAccessToken)

        assertTrue(result.isPresent)
        assertEquals(mockMember.id, result.get().id)
        assertEquals(mockMember.email, result.get().email)
    }

    @Test
    @DisplayName("잘못된 Access Token을 사용하면 Optional.empty()를 반환한다.")
    fun `getMemberByAccessToken should return empty when token is invalid`() {
        `when`(authTokenService.getPayload(invalidAccessToken)).thenReturn(null)

        val result = authService.getMemberByAccessToken(invalidAccessToken)

        assertTrue(result.isEmpty)
    }

    @Test
    @DisplayName("정상적인 Refresh Token을 사용하여 회원 정보를 조회할 수 있다.")
    fun `getMemberByRefreshToken should return member when token is valid`() {
        val payload: Map<String, Any> = mapOf("id" to mockMember.id!!)
        `when`(authTokenService.getPayload(validRefreshToken)).thenReturn(payload)
        `when`(memberRepository.findById(mockMember.id!!)).thenReturn(Optional.of(mockMember))

        val result = authService.getMemberByRefreshToken(validRefreshToken)

        assertTrue(result.isPresent)
        assertEquals(mockMember.id, result.get().id)
    }

    @Test
    @DisplayName("잘못된 Refresh Token을 사용하면 Optional.empty()를 반환한다.")
    fun `getMemberByRefreshToken should return empty when token is invalid`() {
        `when`(authTokenService.getPayload(invalidRefreshToken)).thenReturn(null)

        val result = authService.getMemberByRefreshToken(invalidRefreshToken)

        assertTrue(result.isEmpty)
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID가 포함된 Refresh Token을 사용하면 Optional.empty()를 반환한다.")
    fun `getMemberByRefreshToken should return empty when member not found`() {
        val payload = mapOf("id" to 999L)
        `when`(authTokenService.getPayload(validRefreshToken)).thenReturn(payload)
        `when`(memberRepository.findById(999L)).thenReturn(Optional.empty())

        val result = authService.getMemberByRefreshToken(validRefreshToken)

        assertTrue(result.isEmpty)
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 getMemberByRefreshTokenOrThrow 호출 시 예외가 발생해야 한다.")
    fun `getMemberByRefreshTokenOrThrow should throw exception when member not found`() {
        val payload = mapOf("id" to 999L)
        `when`(authTokenService.getPayload(validRefreshToken)).thenReturn(payload)
        `when`(memberRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<ServiceException> {
            authService.getMemberByRefreshTokenOrThrow(validRefreshToken)
        }

        assertEquals("401", exception.code)
        assertEquals("유효하지 않은 리프레시 토큰이거나 회원을 찾을 수 없습니다.", exception.message)
    }

    @Test
    @DisplayName("정상적인 Access Token이 생성되어야 한다.")
    fun `genAccessToken should return token`() {
        `when`(authTokenService.genAccessToken(mockMember)).thenReturn("newAccessToken")

        val token = authService.genAccessToken(mockMember)

        assertEquals("newAccessToken", token)
    }

    @Test
    @DisplayName("정상적인 Refresh Token이 생성되어야 한다.")
    fun `genRefreshToken should return token`() {
        `when`(authTokenService.genRefreshToken(mockMember.id!!)).thenReturn("newRefreshToken")

        val token = authService.genRefreshToken(mockMember)

        assertEquals("newRefreshToken", token)
    }
}