package com.project2.domain.chat.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.project2.domain.member.entity.Member
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class ChatMessage() : BaseTime() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    @JsonIgnore
    lateinit var chatRoom: ChatRoom

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    lateinit var sender: Member

    @Column(nullable = false, columnDefinition = "TEXT")
    lateinit var content: String

    companion object {
        @JvmStatic
        fun builder() = ChatMessageBuilder()
    }

    class ChatMessageBuilder {
        private lateinit var chatRoom: ChatRoom
        private lateinit var sender: Member
        private lateinit var content: String
        fun chatRoom(chatRoom: ChatRoom) = apply { this.chatRoom = chatRoom }
        fun sender(sender: Member) = apply { this.sender = sender }
        fun content(content: String) = apply { this.content = content }
        fun build() = ChatMessage().apply {
            this.chatRoom = this@ChatMessageBuilder.chatRoom
            this.sender = this@ChatMessageBuilder.sender
            this.content = this@ChatMessageBuilder.content
        }
    }
}