package com.project3.domain.notification.controller

import com.project3.global.security.Rq
import com.project3.global.service.SseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications/subscribe")
class NotificationSseController(
        private val sseService: SseService,
        private val rq: Rq
) {
    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): SseEmitter {
        val actor = rq.getActor()
        return sseService.subscribe(actor.id!!)
    }
}
