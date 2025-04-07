package com.project2.domain.post.mapper

import com.project2.domain.member.entity.Member
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.dto.comment.CommentResponseDTO
import com.project2.domain.post.entity.Comment
import com.project2.domain.post.entity.Post
import org.springframework.stereotype.Component

@Component
class CommentMapper {

    fun toEntity(request: CommentRequestDTO, post: Post, member: Member, parent: Comment?): Comment {
        val depth = if (parent == null) 0 else 1
        return Comment().apply {
            content = request.content
            this.post = post
            this.member = member
            this.parent = parent
            this.depth = depth
        }
    }

    fun toResponseDTO(comment: Comment, nickname: String): CommentResponseDTO {
        return CommentResponseDTO(
            id = comment.id,
            content = comment.content,
            nickname = nickname,
            parentId = comment.parent?.id
        )
    }
}
