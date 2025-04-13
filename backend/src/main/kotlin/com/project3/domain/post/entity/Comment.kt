package com.project3.domain.post.entity

import com.project3.domain.member.entity.Member
import com.project3.global.entity.BaseTime
import com.project3.global.exception.ServiceException
import jakarta.persistence.*

@Entity
class Comment : BaseTime() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    var id: Long? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    lateinit var content: String

    var depth: Int = 0

    @ManyToOne
    @JoinColumn(nullable = false)
    lateinit var post: Post

    @ManyToOne
    @JoinColumn(nullable = false)
    lateinit var member: Member

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    var parent: Comment? = null

    fun updateContent(newContent: String) {
        if (newContent.isBlank()) {
            throw ServiceException("400", "댓글 내용은 비어 있을 수 없습니다.")
        }
        this.content = newContent
    }
}