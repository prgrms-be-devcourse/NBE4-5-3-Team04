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
}