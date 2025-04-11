package com.project2.domain.notification.event

import com.project2.domain.member.entity.Member
import com.project2.domain.notification.enums.NotificationType

data class NotificationEvent(
        val receiver: Member,
        val sender: Member,
        val type: NotificationType,
        val content: String,
        val relatedId: Long
)