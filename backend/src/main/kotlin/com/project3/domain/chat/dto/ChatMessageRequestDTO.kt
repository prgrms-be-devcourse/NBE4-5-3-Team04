package com.project3.domain.chat.dto

import java.util.*

data class ChatMessageRequestDTO(
        var chatRoomId: UUID = UUID.randomUUID(),
        var content: String = ""
)
