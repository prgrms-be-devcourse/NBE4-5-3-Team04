package com.project3.domain.notification.repository

import com.project3.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    @EntityGraph(attributePaths = ["sender", "receiver"])
    fun findByReceiverIdAndReadFalseOrderByCreatedDateDesc(receiverId: Long): List<Notification>

    @EntityGraph(attributePaths = ["sender", "receiver"])
    override fun findById(id: Long): java.util.Optional<Notification>
}
