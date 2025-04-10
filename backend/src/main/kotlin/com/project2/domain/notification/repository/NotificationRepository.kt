package com.project2.domain.notification.repository

import com.project2.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByReceiverIdAndReadFalseOrderByCreatedDateDesc(receiverId: Long): List<Notification>
}
