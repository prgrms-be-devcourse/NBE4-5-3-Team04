package com.project2.domain.notification.service

import com.project2.domain.member.entity.Member
import com.project2.domain.notification.dto.NotificationResponseDTO
import com.project2.domain.notification.entity.Notification
import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.repository.NotificationRepository
import com.project2.global.service.SseService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
        private val notificationRepository: NotificationRepository,
        private val sseService: SseService
) {
    @Transactional
    fun createNotification(
            receiver: Member,
            sender: Member,
            type: NotificationType,
            content: String,
            relatedId: Long
    ): Notification {
        val notification = Notification(
                receiver = receiver,
                sender = sender,
                type = type,
                content = content,
                relatedId = relatedId
        )
        val savedNotification = notificationRepository.save(notification)

        // SSE를 통해 실시간 알림 전송
        try {
            val responseDTO = NotificationResponseDTO.from(savedNotification)
            sseService.sendToUser(receiver.id!!, responseDTO)
        } catch (e: Exception) {
            // SSE 전송 실패 예외 처리 (알림 저장은 성공했으므로 로그만 출력)
            println("알림 전송 실패: ${e.message}")
        }

        return savedNotification
    }

    @Transactional(readOnly = true)
    fun getUnreadNotifications(memberId: Long): List<Notification> {
        return notificationRepository.findByReceiverIdAndReadFalseOrderByCreatedDateDesc(memberId)
    }

    @Transactional
    fun markAsRead(notificationId: Long) {
        val notification = notificationRepository.findById(notificationId).orElseThrow()
        notification.read = true
        notificationRepository.save(notification)
    }
}
