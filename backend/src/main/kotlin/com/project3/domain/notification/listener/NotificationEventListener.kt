package com.project3.domain.notification.listener

import com.project3.domain.notification.dto.NotificationResponseDTO
import com.project3.domain.notification.entity.Notification
import com.project3.domain.notification.event.NotificationEvent
import com.project3.domain.notification.repository.NotificationRepository
import com.project3.global.service.SseService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NotificationEventListener(
        private val notificationRepository: NotificationRepository,
        private val sseService: SseService
) {
    @Async
    @EventListener
    fun handle(event: NotificationEvent) {
        val notification = Notification(
                receiver = event.receiver,
                sender = event.sender,
                type = event.type,
                content = event.content,
                relatedId = event.relatedId
        )
        val saved = notificationRepository.save(notification)
        val dto = NotificationResponseDTO.from(saved)
        sseService.sendToUser(event.receiver.id!!, dto)
    }
}