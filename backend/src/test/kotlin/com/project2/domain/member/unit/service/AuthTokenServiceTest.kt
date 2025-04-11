package com.project2.domain.member.unit.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.service.AuthTokenService
import com.project2.global.util.Ut
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Field

@Transactional
@ExtendWith(MockitoExtension::class)
class AuthTokenServiceTest {

    private val keyString = "abcdefghijklmnopqrstuvwxyz123456"
    private val accessTokenExpireSeconds = 3600

    private lateinit var authTokenService: AuthTokenService

    @BeforeEach
    fun setUp() {
        authTokenService = AuthTokenService()
        injectValue(authTokenService, "keyString", keyString)
        injectValue(authTokenService, "accessTokenExpireSeconds", accessTokenExpireSeconds)
    }

    private fun injectValue(target: Any, fieldName: String, value: Any) {
        val field: Field = target::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    @Test
    @DisplayName("jwt 생성")
    fun `create token`() {
        val originPayload = mapOf("name" to "john", "age" to 23)

        val jwtStr = Ut.Jwt.createToken(keyString, accessTokenExpireSeconds, originPayload)
        assertThat(jwtStr).isNotBlank()

        val parsedPayload = Ut.Jwt.getPayload(keyString, jwtStr)
        assertThat(parsedPayload).containsAllEntriesOf(originPayload)
    }

    @Test
    @DisplayName("access token 생성")
    fun `generate access token`() {
        val mockMember = Member().apply {
            id = 1L
            email = "test@test.com"
            provider = Provider.NAVER
        }

        val accessToken = authTokenService.genAccessToken(mockMember)
        assertThat(accessToken).isNotBlank()
    }

    @Test
    @DisplayName("jwt 유효성 검사")
    fun `validate jwt`() {
        val mockMember = Member().apply {
            id = 1L
            email = "test@test.com"
            nickname = "nickname"
            provider = Provider.NAVER
        }

        val accessToken = authTokenService.genAccessToken(mockMember)

        val isValid = Ut.Jwt.isValidToken(keyString, accessToken)
        assertThat(isValid).isTrue()

        val parsedPayload = authTokenService.getPayload(accessToken)

        assertThat(parsedPayload).containsEntry("id", mockMember.id)
        assertThat(parsedPayload).containsEntry("email", mockMember.email)
    }
}
