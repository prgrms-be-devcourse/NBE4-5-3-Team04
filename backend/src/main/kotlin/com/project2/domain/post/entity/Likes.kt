package com.project2.domain.post.entity

import com.project2.domain.member.entity.Member
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Likes : BaseTime() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    lateinit var post: Post

    @ManyToOne
    @JoinColumn(nullable = false)
    lateinit var member: Member

    companion object {
        @JvmStatic
        fun builder() = LikesBuilder()
    }

    class LikesBuilder {
        private var id: Long? = null
        private lateinit var post: Post
        private lateinit var member: Member

        fun id(id: Long) = apply { this.id = id }
        fun post(post: Post) = apply { this.post = post }
        fun member(member: Member) = apply { this.member = member }

        fun build() = Likes().apply {
            this.id = this@LikesBuilder.id!!
            this.post = this@LikesBuilder.post
            this.member = this@LikesBuilder.member
        }
    }
}