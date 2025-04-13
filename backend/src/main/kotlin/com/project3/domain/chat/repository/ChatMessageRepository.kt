package com.project3.domain.chat.repository

import com.project3.domain.chat.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    @Query(
            value = "SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId",
            countQuery = "SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId"
    )
    @EntityGraph(attributePaths = ["chatRoom", "sender"])
    fun findByChatRoomId(@Param("roomId") roomId: UUID, pageable: Pageable): Page<ChatMessage>
}