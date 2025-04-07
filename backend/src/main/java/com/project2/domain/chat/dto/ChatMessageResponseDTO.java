package com.project2.domain.chat.dto;

import com.project2.domain.chat.entity.ChatMessage;
import com.project2.domain.member.dto.MemberDTO;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponseDTO {
    public final Long id;
    private final MemberDTO sender;
    private final String content;
    private final LocalDateTime createdAt;

    public ChatMessageResponseDTO(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.sender = MemberDTO.Companion.from(chatMessage.getSender());
        this.content = chatMessage.getContent();
        this.createdAt = chatMessage.getCreatedDate();
    }

}
