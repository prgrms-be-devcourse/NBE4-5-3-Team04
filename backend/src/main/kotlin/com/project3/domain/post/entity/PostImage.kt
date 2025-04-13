package com.project3.domain.post.entity

import com.project3.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class PostImage : BaseTime() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, length = 500)
    lateinit var imageUrl: String

    @ManyToOne
    @JoinColumn(nullable = false)
    lateinit var post: Post
}