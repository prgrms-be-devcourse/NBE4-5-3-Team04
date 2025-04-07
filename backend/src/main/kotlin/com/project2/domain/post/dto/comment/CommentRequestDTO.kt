package com.project2.domain.post.dto.comment

class CommentRequestDTO(
    val content: String,
    val parentId: Long?
)