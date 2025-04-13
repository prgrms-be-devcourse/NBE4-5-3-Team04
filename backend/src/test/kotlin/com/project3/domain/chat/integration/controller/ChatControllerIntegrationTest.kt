package com.project3.domain.chat.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.project3.domain.chat.dto.ChatMessageRequestDTO
import com.project3.domain.chat.service.ChatService
import com.project3.domain.member.entity.Member
import com.project3.domain.member.enums.Provider
import com.project3.domain.member.repository.MemberRepository
import com.project3.domain.member.service.AuthTokenService
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var chatService: ChatService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var member: Member
    private lateinit var accessToken: String

    companion object {
        private const val DEFAULT_EMAIL = "test@test.com"
        private const val DEFAULT_NICKNAME = "tester"
    }

    @BeforeEach
    fun setUp() {
        member = createMember(DEFAULT_EMAIL, DEFAULT_NICKNAME)
        accessToken = authTokenService.genAccessToken(member)
    }

    private fun createMember(email: String, nickname: String): Member {
        return memberRepository.save(
                Member(email = email, nickname = nickname, provider = Provider.NAVER)
        )
    }

    @Test
    @DisplayName("채팅방 목록 조회 - DB에 미리 채팅방을 생성하고 테스트")
    fun `should return chat rooms when chat room exists`() {
        val opponent = createMember("opponent@test.com", "상대방")

        chatService.findOrCreateChatRoomId(member.id!!, opponent.id!!)

        mockMvc.get("/api/chat/rooms") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("성공") }
            jsonPath("$.data[0].opponent.nickname") { value("상대방") }
        }
    }

    @Test
    @DisplayName("채팅 메시지 전송 - 성공")
    fun `should send message and return sender info`() {
        val opponent = createMember("oppo2@test.com", "상대방2")

        val chatRoomId = chatService.findOrCreateChatRoomId(member.id!!, opponent.id!!)
        val messageContent = "안녕하세요"
        val requestDTO = ChatMessageRequestDTO(chatRoomId = chatRoomId, content = messageContent)
        val jsonBody = objectMapper.writeValueAsString(requestDTO)

        mockMvc.post("/api/chat/send") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "UTF-8"
            content = jsonBody
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.msg") { value("성공") }
            jsonPath("$.data.content") { value(messageContent) }
            jsonPath("$.data.sender.nickname") { value(DEFAULT_NICKNAME) }
            jsonPath("$.data.id") { exists() }
        }
    }
}
