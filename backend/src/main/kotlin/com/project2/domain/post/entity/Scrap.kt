package com.project2.domain.post.entity

import com.project2.domain.member.entity.Member
import com.project2.global.entity.BaseTime
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