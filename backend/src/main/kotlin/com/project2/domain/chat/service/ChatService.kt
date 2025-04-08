package com.project2.domain.chat.service

import com.project2.domain.chat.dto.ChatMessageResponseDTO
import com.project2.domain.chat.dto.ChatRoomResponseDTO
import com.project2.domain.chat.entity.ChatMessage
import com.project2.domain.chat.entity.ChatRoom
import com.project2.domain.chat.repository.ChatMessageRepository
import com.project2.domain.chat.repository.ChatRoomRepository
import com.project2.domain.member.repository.MemberRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ChatService(
        private val chatMessageRepository: ChatMessageRepository,
        private val chatRoomRepository: ChatRoomRepository,
        private val memberRepository: MemberRepository
) {

    @Transactional
    fun findOrCreateChatRoomId(myId: Long, opponentId: Long): UUID {
        val existingRoomId = chatRoomRepository.findChatRoomIdByMemberIds(myId, opponentId)
        if (existingRoomId.isPresent) {
            return existingRoomId.get()
        }

        val me = memberRepository.findById(myId)
                .orElseThrow { RuntimeException("내 정보를 찾을 수 없습니다.") }

        val opponent = memberRepository.findById(opponentId)
                .orElseThrow { RuntimeException("상대 정보를 찾을 수 없습니다.") }

        val newChatRoom = ChatRoom().apply {
            members = mutableSetOf(me, opponent)
        }

        return chatRoomRepository.save(newChatRoom).id!!
    }

    @Transactional(readOnly = true)
    fun getAllChatRooms(actorId: Long): List<ChatRoomResponseDTO> {
        return chatRoomRepository.findByMembers_IdOrderByLatestMessage(actorId)
                .map { ChatRoomResponseDTO.from(it, actorId) }
    }

    @Transactional(readOnly = true)
    fun getChatMessages(roomId: UUID, pageable: Pageable): Page<ChatMessageResponseDTO> {
        return chatMessageRepository.findByChatRoomId(roomId, pageable)
                .map(ChatMessageResponseDTO::from)
    }

    @Transactional
    fun sendMessage(actorId: Long, chatRoomId: UUID, content: String): ChatMessageResponseDTO {
        val actor = memberRepository.getReferenceById(actorId)
        val chatRoom = chatRoomRepository.getReferenceById(chatRoomId)

        val chatMessage = ChatMessage.builder()
                .sender(actor)
                .chatRoom(chatRoom)
                .content(content)
                .build()

        val saved = chatMessageRepository.save(chatMessage)
        return ChatMessageResponseDTO.from(saved)
    }
}