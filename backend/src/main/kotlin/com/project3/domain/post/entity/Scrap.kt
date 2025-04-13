package com.project3.domain.post.entity

import com.project3.domain.member.entity.Member
import com.project3.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Scrap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(nullable = false)
    var post: Post,

    @ManyToOne
    @JoinColumn(nullable = false)
    var member: Member
) : BaseTime()