package com.project2.domain.notification.controller

import com.project2.domain.notification.dto.NotificationResponseDTO
import com.project2.domain.notification.service.NotificationService
import com.project2.global.dto.RsData
import com.project2.global.security.Rq
import com.project2.global.service.SseService
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
        private val notificationService: NotificationService,
        private val rq: Rq,
        private val sseService: SseService
) {
    @GetMapping
    fun getUnreadNotifications(): RsData<List<NotificationResponseDTO>> {
        val actor = rq.getActor()
        val notifications = notificationService.getUnreadNotifications(actor.id!!)
        val responseDTO = notifications.map { NotificationResponseDTO.from(it) }
        return RsData("200", "알림 목록을 성공적으로 조회했습니다.", responseDTO)
    }

    @PatchMapping("/{notificationId}/read")
    fun markAsRead(@PathVariable notificationId: Long): RsData<Unit> {
        notificationService.markAsRead(notificationId)
        return RsData("200", "알림을 읽음 처리했습니다.")
    }

    @GetMapping("/subscribe")
    fun subscribe(@RequestParam userId: Long): SseEmitter {
        return sseService.subscribe(userId)
    }
}
