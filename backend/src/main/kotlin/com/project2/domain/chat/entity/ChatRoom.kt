package com.project2.domain.chat.entity

import com.project2.domain.member.entity.Member
import com.project2.global.entity.BaseTime
import jakarta.persistence.*
import java.util.*

@Entity
class ChatRoom() : BaseTime() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ManyToMany
    @JoinTable(
            name = "chat_room_member",
            joinColumns = [JoinColumn(name = "chat_room_id")],
            inverseJoinColumns = [JoinColumn(name = "member_id")]
    )
    var members: MutableSet<Member> = mutableSetOf()

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var messages: MutableSet<ChatMessage> = mutableSetOf()

    companion object {
        @JvmStatic
        fun builder() = ChatRoomBuilder()
    }

    class ChatRoomBuilder {
        private var id: UUID? = null
        private var members: MutableSet<Member> = mutableSetOf()
        private var messages: MutableSet<ChatMessage> = mutableSetOf()

        fun id(id: UUID?) = apply { this.id = id }
        fun members(members: MutableSet<Member>) = apply { this.members = members }
        fun messages(messages: MutableSet<ChatMessage>) = apply { this.messages = messages }

        fun build() = ChatRoom().apply {
            this.id = this@ChatRoomBuilder.id
            this.members = this@ChatRoomBuilder.members
            this.messages = this@ChatRoomBuilder.messages
        }
    }
}