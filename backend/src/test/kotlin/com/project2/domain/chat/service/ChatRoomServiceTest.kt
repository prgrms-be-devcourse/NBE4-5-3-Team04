package com.project2.domain.chat.service

import com.project2.domain.chat.dto.ChatRoomResponseDTO
import com.project2.domain.chat.entity.ChatMessage
import com.project2.domain.chat.entity.ChatRoom
import com.project2.domain.chat.repository.ChatMessageRepository
import com.project2.domain.chat.repository.ChatRoomRepository
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*

class ChatServiceTest {

    private lateinit var chatMessageRepository: ChatMessageRepository
    private lateinit var chatRoomRepository: ChatRoomRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var chatService: ChatService

    @BeforeEach
    fun setup() {
        chatMessageRepository = mockk()
        chatRoomRepository = mockk()
        memberRepository = mockk()
        chatService = ChatService(chatMessageRepository, chatRoomRepository, memberRepository)
    }

    @Test
    fun `should return existing chat room id if room already exists`() {
        // given
        val myId = 1L
        val opponentId = 2L
        val existingRoomId = UUID.randomUUID()

        every {
            chatRoomRepository.findChatRoomIdByMemberIds(myId, opponentId)
        } returns Optional.of(existingRoomId)

        // when
        val result = chatService.findOrCreateChatRoomId(myId, opponentId)

        // then
        assertEquals(existingRoomId, result)
        verify(exactly = 0) { memberRepository.findById(any()) }
    }

    @Test
    fun `should create new chat room if no existing room found`() {
        // given
        val myId = 1L
        val opponentId = 2L
        val me = Member().apply { id = myId }
        val opponent = Member().apply { id = opponentId }
        val savedRoom = ChatRoom().apply {
            id = UUID.randomUUID()
            members = mutableSetOf(me, opponent)
        }

        every { chatRoomRepository.findChatRoomIdByMemberIds(myId, opponentId) } returns Optional.empty()
        every { memberRepository.findById(myId) } returns Optional.of(me)
        every { memberRepository.findById(opponentId) } returns Optional.of(opponent)
        every { chatRoomRepository.save(any()) } returns savedRoom

        // when
        val result = chatService.findOrCreateChatRoomId(myId, opponentId)

        // then
        assertEquals(savedRoom.id, result)
    }

    @Test
    fun `should return all chat rooms for a member`() {
        // given
        val actorId = 1L
        val room1 = ChatRoom().apply { id = UUID.randomUUID(); members = mutableSetOf(Member().apply { id = actorId }, Member().apply { id = 2L }) }
        val room2 = ChatRoom().apply { id = UUID.randomUUID(); members = mutableSetOf(Member().apply { id = actorId }, Member().apply { id = 3L }) }

        every { chatRoomRepository.findByMembers_IdOrderByLatestMessage(actorId) } returns listOf(room1, room2)

        // when
        val result = chatService.getAllChatRooms(actorId)

        // then
        assertEquals(2, result.size)
        assertTrue(result.all { it is ChatRoomResponseDTO })
    }

    @Test
    fun `should return paginated chat messages for a room`() {
        // given
        val roomId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)

        val chatRoom = ChatRoom().apply { id = roomId }
        val sender = Member().apply { id = 1L }

        val message = ChatMessage().apply {
            this.id = 1L
            this.chatRoom = chatRoom
            this.sender = sender
            this.content = "Hello"
            this.createdDate = LocalDateTime.now()
        }
        every { chatMessageRepository.findByChatRoomId(roomId, pageable) } returns PageImpl(listOf(message))

        // when
        val result = chatService.getChatMessages(roomId, pageable)

        // then
        assertEquals(1, result.totalElements)
        assertEquals("Hello", result.content.first().content)
    }

    @Test
    fun `should send a message and return response dto`() {
        // given
        val actorId = 1L
        val chatRoomId = UUID.randomUUID()
        val content = "Hi!"
        val actor = Member().apply { id = actorId }
        val chatRoom = ChatRoom()
        val message = ChatMessage().apply {
            this.id = 1L
            this.sender = actor
            this.chatRoom = chatRoom
            this.content = content
            this.createdDate = LocalDateTime.now()
        }

        every { memberRepository.getReferenceById(actorId) } returns actor
        every { chatRoomRepository.getReferenceById(chatRoomId) } returns chatRoom
        every { chatMessageRepository.save(any()) } returns message

        // when
        val result = chatService.sendMessage(actorId, chatRoomId, content)

        // then
        assertEquals(content, result.content)
        assertEquals(actorId, result.sender.id)
    }
}