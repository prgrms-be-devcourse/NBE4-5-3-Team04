package com.project2.domain.notification.dto

import com.project2.domain.notification.entity.Notification
import com.project2.domain.notification.enums.NotificationType
import java.time.LocalDateTime

data class NotificationResponseDTO(
        val id: Long,
        val type: NotificationType,
        val content: String,
        val senderId: Long,
        val senderNickname: String,
        val relatedId: Long,
        val isRead: Boolean,
        val createdAt: LocalDateTime
) {
    companion object {
        @JvmStatic
        fun from(notification: Notification): NotificationResponseDTO {
            return NotificationResponseDTO(
                    id = notification.id!!,
                    type = notification.type,
                    content = notification.content,
                    senderId = notification.sender.id!!,
                    senderNickname = notification.sender.nickname,
                    relatedId = notification.relatedId,
                    isRead = notification.read,
                    createdAt = notification.createdDate
            )
        }
    }
}
