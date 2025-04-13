package com.project3.domain.notification.event

import com.project3.domain.member.entity.Member
import com.project3.domain.notification.enums.NotificationType

data class NotificationEvent(
        val receiver: Member,
        val sender: Member,
        val type: NotificationType,
        val content: String,
        val relatedId: Long
)