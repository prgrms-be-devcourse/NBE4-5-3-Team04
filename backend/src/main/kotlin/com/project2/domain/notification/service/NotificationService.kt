package com.project2.domain.notification.service

import com.project2.domain.notification.dto.NotificationEventDTO
import com.project2.domain.notification.dto.NotificationResponseDTO
import com.project2.domain.notification.entity.Notification
import com.project2.domain.notification.event.NotificationEvent
import com.project2.domain.notification.repository.NotificationRepository
import com.project2.global.service.SseService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
        private val notificationRepository: NotificationRepository,
        private val sseService: SseService
) {

    @Transactional(readOnly = true)
    fun getUnreadNotifications(memberId: Long): List<Notification> {
        return notificationRepository.findByReceiverIdAndReadFalseOrderByCreatedDateDesc(memberId)
    }

    @Transactional(readOnly = true)
    fun getUnreadNotificationsDTO(memberId: Long): List<NotificationResponseDTO> {
        val notifications = getUnreadNotifications(memberId)
        return notifications.map { NotificationResponseDTO.from(it) }
    }

    @Transactional
    fun markAsRead(notificationId: Long) {
        val notification = notificationRepository.findById(notificationId).orElseThrow()
        notification.read = true
        notificationRepository.save(notification)
    }

    /**
     * 비동기로 알림 이벤트 처리
     * @param event 알림 이벤트
     */
    @Async
    @Transactional
    fun processNotificationAsync(event: NotificationEvent) {
        try {
            // 알림 엔티티 생성 및 저장
            val notification = Notification(
                    sender = event.sender,
                    receiver = event.receiver,
                    content = event.content,
                    type = event.type,
                    relatedId = event.relatedId
            )
            val savedNotification = notificationRepository.save(notification)
            println("알림 저장 성공: ${savedNotification.id}")

            // NotificationEventDTO로 변환하여 SSE 메시지 전송
            val eventDTO = NotificationEventDTO.from(event)
            sseService.sendToUser(event.receiver.id!!, eventDTO)
        } catch (e: Exception) {
            println("알림 처리 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
    }
}
