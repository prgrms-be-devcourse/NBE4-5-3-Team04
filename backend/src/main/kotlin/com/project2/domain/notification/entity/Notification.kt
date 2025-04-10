package com.project2.domain.notification.entity

import com.project2.domain.member.entity.Member
import com.project2.domain.notification.enums.NotificationType
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Notification(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_id")
        val receiver: Member,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_id")
        val sender: Member,

        @Enumerated(EnumType.STRING)
        val type: NotificationType,

        val content: String,

        @Column(name = "related_id")
        val relatedId: Long,

        @Column(name = "is_read")
        var read: Boolean = false

) : BaseTime()
