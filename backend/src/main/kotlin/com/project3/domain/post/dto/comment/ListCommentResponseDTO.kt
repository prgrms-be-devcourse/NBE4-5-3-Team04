package com.project3.domain.post.dto.comment

class ListCommentResponseDTO(
    val id: Long,
    val content: String,
    val nickname: String,
    val parentId: Long?,
    val children: MutableList<ListCommentResponseDTO> = mutableListOf()
) {
    constructor(id: Long, content: String, nickname: String, parentId: Long?) : this(
        id, content, nickname, parentId, mutableListOf()
    )
}