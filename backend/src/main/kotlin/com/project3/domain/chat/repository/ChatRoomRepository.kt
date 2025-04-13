package com.project3.domain.chat.repository

import com.project3.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ChatRoomRepository : JpaRepository<ChatRoom, UUID> {

    @Query("""
        SELECT cr.id FROM ChatRoom cr
        JOIN cr.members m
        WHERE m.id IN (:myId, :opponentId)
        GROUP BY cr.id
        HAVING COUNT(DISTINCT m.id) = 2
""")
    fun findChatRoomIdByMemberIds(
            @Param("myId") myId: Long,
            @Param("opponentId") opponentId: Long
    ): Optional<UUID>

    @Query("""
        SELECT cr FROM ChatRoom cr 
        JOIN cr.members m 
        WHERE m.id in :actorId 
        ORDER BY cr.createdDate DESC
    """)
    fun findByMembers_IdOrderByLatestMessage(@Param("actorId") actorId: Long): List<ChatRoom>
}