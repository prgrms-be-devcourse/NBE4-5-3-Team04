package com.project2.domain.post.dto.comment;

import lombok.Getter;

@Getter
public class CommentResponseDTO {
    private final Long id;
    private final String content;
    private final String nickname;
    private final Long parentId;

    public CommentResponseDTO(Long id, String content, String nickname, Long parentId) {
        this.id = id;
        this.content = content;
        this.nickname = nickname;
        this.parentId = parentId;
    }
}