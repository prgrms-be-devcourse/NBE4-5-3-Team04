package com.project2.domain.notification.dto

import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.event.NotificationEvent

data class NotificationEventDTO(
        val id: Long? = null,
        val senderId: Long,
        val senderNickname: String,
        val receiverId: Long,
        val type: NotificationType,
        val content: String,
        val relatedId: Long,
        val isRead: Boolean = false,
        val createdAt: String? = null
) {
    companion object {
        fun from(event: NotificationEvent): NotificationEventDTO {
            return NotificationEventDTO(
                    senderId = event.sender.id!!,
                    senderNickname = event.sender.nickname,
                    receiverId = event.receiver.id!!,
                    type = event.type,
                    content = event.content,
                    relatedId = event.relatedId
            )
        }
    }
}
