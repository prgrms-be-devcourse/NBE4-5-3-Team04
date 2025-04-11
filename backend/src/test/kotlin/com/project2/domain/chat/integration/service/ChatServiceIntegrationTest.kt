package com.project2.domain.chat.integration.service

import com.project2.domain.chat.service.ChatService
import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ChatServiceIntegrationTest {

    @Autowired
    lateinit var chatService: ChatService

    @Autowired
    lateinit var memberRepository: MemberRepository

    private lateinit var memberA: Member
    private lateinit var memberB: Member

    @BeforeEach
    fun setUp() {
        memberA = memberRepository.save(
                Member(
                        email = "userA@example.com",
                        nickname = "UserA",
                        provider = Provider.GOOGLE
                )
        )

        memberB = memberRepository.save(
                Member(
                        email = "userB@example.com",
                        nickname = "UserB",
                        provider = Provider.GOOGLE
                )
        )
    }

    @Test
    @DisplayName("채팅방이 없을 때 생성하고 UUID 반환")
    fun `should create chat room if not exists`() {
        val roomId = chatService.findOrCreateChatRoomId(memberA.id!!, memberB.id!!)
        assertThat(roomId).isNotNull
    }

    @Test
    @DisplayName("동일한 멤버 쌍에 대해 기존 채팅방 UUID 반환")
    fun `should return existing chat room id`() {
        val roomId1 = chatService.findOrCreateChatRoomId(memberA.id!!, memberB.id!!)
        val roomId2 = chatService.findOrCreateChatRoomId(memberA.id!!, memberB.id!!)
        assertThat(roomId1).isEqualTo(roomId2)
    }

    @Test
    @DisplayName("메시지 전송 후 해당 채팅방에서 메시지 조회")
    fun `should send and fetch chat message`() {
        val roomId = chatService.findOrCreateChatRoomId(memberA.id!!, memberB.id!!)
        chatService.sendMessage(memberA.id!!, roomId, "Hello")

        val messages = chatService.getChatMessages(roomId, PageRequest.of(0, 10))
        assertThat(messages.content).hasSize(1)
        assertThat(messages.content[0].content).isEqualTo("Hello")
    }

    @Test
    @DisplayName("채팅방 목록 조회 시 상대방 닉네임 포함")
    fun `should get chat room list with opponent`() {
        chatService.findOrCreateChatRoomId(memberA.id!!, memberB.id!!)
        val rooms = chatService.getAllChatRooms(memberA.id!!)
        assertThat(rooms).hasSize(1)
        assertThat(rooms[0].opponent.nickname).isEqualTo("UserB")
    }
}
