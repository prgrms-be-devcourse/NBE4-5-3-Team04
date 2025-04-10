package com.project2.global.service

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Service
class SseService {
    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    fun subscribe(userId: Long): SseEmitter {
        val emitter = SseEmitter(60_000L)
        emitters[userId] = emitter
        emitter.onCompletion { emitters.remove(userId) }
        emitter.onTimeout { emitters.remove(userId) }
        return emitter
    }

    fun sendToUser(userId: Long, data: Any) {
        emitters[userId]?.send(SseEmitter.event().name("notification").data(data))
    }
}