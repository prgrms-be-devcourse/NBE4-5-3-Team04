package com.project3.domain.chat.controller

import com.project3.domain.chat.dto.ChatMessageRequestDTO
import com.project3.domain.chat.dto.ChatMessageResponseDTO
import com.project3.domain.chat.dto.ChatRoomResponseDTO
import com.project3.domain.chat.service.ChatService
import com.project3.global.dto.RsData
import com.project3.global.security.SecurityUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
        private val chatService: ChatService,
        private val messagingTemplate: SimpMessagingTemplate
) {

    @GetMapping("/rooms")
    fun getAllChatRooms(@AuthenticationPrincipal actor: SecurityUser): RsData<List<ChatRoomResponseDTO>> {
        return RsData("200", "성공", chatService.getAllChatRooms(actor.id))
    }

    @GetMapping("/rooms/find/{opponentId}")
    fun getOrCreateChatRoomId(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable opponentId: Long
    ): RsData<UUID> {
        return RsData("200", "성공", chatService.findOrCreateChatRoomId(actor.id, opponentId))
    }

    @GetMapping("/rooms/{roomId}")
    fun getChatMessages(
            @PathVariable roomId: UUID,
            @RequestParam(defaultValue = "0") offset: Int,
            @RequestParam(defaultValue = "10") size: Int
    ): RsData<Page<ChatMessageResponseDTO>> {
        val pageable: Pageable = PageRequest.of(offset / size, size, Sort.by("createdDate").ascending())
        return RsData("200", "성공", chatService.getChatMessages(roomId, pageable))
    }

    @PostMapping("/send")
    fun sendMessage(
            @AuthenticationPrincipal actor: SecurityUser,
            @RequestBody request: ChatMessageRequestDTO
    ): RsData<ChatMessageResponseDTO> {
        val responseDTO = chatService.sendMessage(actor.id, request.chatRoomId, request.content)
        messagingTemplate.convertAndSend("/queue/chatroom/${request.chatRoomId}", responseDTO)
        return RsData("200", "성공", responseDTO)
    }
}