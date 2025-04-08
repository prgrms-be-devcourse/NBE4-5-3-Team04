package com.project2.domain.chat.dto

import com.project2.domain.chat.entity.ChatMessage
import com.project2.domain.member.dto.MemberDTO
import java.time.LocalDateTime

data class ChatMessageResponseDTO(
        val id: Long,
        val sender: MemberDTO,
        val content: String,
        val createdAt: LocalDateTime
) {
    companion object {
        @JvmStatic
        fun from(chatMessage: ChatMessage): ChatMessageResponseDTO {
            return ChatMessageResponseDTO(
                    id = chatMessage.id!!,
                    sender = MemberDTO.from(chatMessage.sender),
                    content = chatMessage.content,
                    createdAt = chatMessage.createdDate
            )
        }
    }
}