package com.project3.domain.chat.dto

import com.project3.domain.chat.entity.ChatRoom
import com.project3.domain.member.entity.Member
import java.util.*

data class ChatRoomResponseDTO(
        val id: UUID,
        val opponent: Member
) {
    companion object {
        @JvmStatic
        fun from(chatRoom: ChatRoom, actorId: Long): ChatRoomResponseDTO {
            val opponent = chatRoom.members
                    .find { it.id != actorId }
                    ?: throw IllegalStateException("ChatRoom must have exactly one opponent")
            val id = requireNotNull(chatRoom.id) { "ChatRoom.id must not be null" }

            return ChatRoomResponseDTO(
                    id = id,
                    opponent = opponent
            )
        }
    }
}