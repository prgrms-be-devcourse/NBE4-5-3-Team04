package com.project2.domain.chat.dto

import java.util.*

data class ChatMessageRequestDTO(
        var chatRoomId: UUID = UUID.randomUUID(),
        var content: String = ""
)
