package com.project2.global.annotation

import com.project2.domain.notification.enums.NotificationType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotifyEvent(
        val type: NotificationType,
        val receiver: String,
        val sender: String,
        val content: String,
        val relatedId: String
)
