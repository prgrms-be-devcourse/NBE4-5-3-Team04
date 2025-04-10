package com.project2.domain.notification.service

import com.project2.domain.member.entity.Member
import com.project2.domain.notification.entity.Notification
import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.repository.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
        private val notificationRepository: NotificationRepository
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
        return notificationRepository.save(notification)
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
