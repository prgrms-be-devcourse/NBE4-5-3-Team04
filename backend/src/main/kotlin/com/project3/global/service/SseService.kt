package com.project3.global.service

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Service
class SseService {
    private val emitters = ConcurrentHashMap<Long, SseEmitter>()
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    // 타임아웃 시간을 10분으로 설정 (600,000ms)
    private val TIMEOUT = 600_000L

    // 하트비트 간격을 10초로 설정 (이전 15초)
    private val HEARTBEAT_INTERVAL = 10L

    fun subscribe(userId: Long): SseEmitter {
        // 기존 emitter 제거
        emitters.remove(userId)?.complete()
        heartbeatTasks.remove(userId)?.cancel(true)

        // 타임아웃 시간을 10분으로 설정
        val emitter = SseEmitter(TIMEOUT)

        // 이벤트 핸들러 등록
        emitter.onCompletion {
            emitters.remove(userId)
            heartbeatTasks.remove(userId)?.cancel(true)
            println("SSE 연결 완료: $userId")
        }

        emitter.onTimeout {
            emitters.remove(userId)
            heartbeatTasks.remove(userId)?.cancel(true)
            println("SSE 연결 타임아웃: $userId")
        }

        emitter.onError { e ->
            emitters.remove(userId)
            heartbeatTasks.remove(userId)?.cancel(true)
            println("SSE 연결 오류: $userId, 오류: ${e.message}")
        }

        // emitter 등록
        emitters[userId] = emitter

        // 초기 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("연결 성공")
                    .reconnectTime(3000))

            // 하트비트 시작 (10초 후부터 30초 간격으로)
            scheduleHeartbeat(userId)

            println("SSE 연결 성공: $userId")
        } catch (e: Exception) {
            emitters.remove(userId)
            heartbeatTasks.remove(userId)?.cancel(true)
            println("SSE 초기 연결 오류: ${e.message}")
        }

        return emitter
    }

    // 사용자별 스케줄러 관리
    private val heartbeatTasks = ConcurrentHashMap<Long, java.util.concurrent.Future<*>>()

    private fun scheduleHeartbeat(userId: Long) {
        // 기존 태스크가 있다면 취소
        heartbeatTasks[userId]?.cancel(true)

        // 새 하트비트 태스크 시작
        val task = scheduler.scheduleAtFixedRate({
            try {
                // emitter 가 존재하는지 확인
                if (emitters.containsKey(userId)) {
                    try {
                        // 안전하게 하트비트 전송 시도
                        emitters[userId]?.send(SseEmitter.event()
                                .name("heartbeat")
                                .data("keep-alive-${System.currentTimeMillis()}")
                                .reconnectTime(3000))
                    } catch (e: Exception) {
                        // 하트비트 전송 실패 시 emitter 제거
                        emitters.remove(userId)
                        heartbeatTasks.remove(userId)?.cancel(true)
                    }
                } else {
                    // emitter가 없으면 태스크 취소
                    println("사용자 emitter 없음, 하트비트 취소: $userId")
                    heartbeatTasks.remove(userId)?.cancel(true)
                }
            } catch (e: Exception) {
                // 예외 발생 시 로그만 기록하고 계속 실행
                println("하트비트 처리 중 예외 발생: ${e.message}")
            }
        }, 2, HEARTBEAT_INTERVAL, java.util.concurrent.TimeUnit.SECONDS)

        // 태스크 저장
        heartbeatTasks[userId] = task
    }

    fun sendToUser(userId: Long, data: Any) {
        try {
            var emitter = emitters[userId]

            // emitter가 없으면 새로 생성
            if (emitter == null) {
                println("알림 전송 시도: $userId 사용자의 emitter 없음, 새로 생성 시도")
                try {
                    // 새 emitter 생성
                    emitter = subscribe(userId)
                    println("새 emitter 생성 성공: $userId")

                    // 연결 안정화를 위해 잠시 대기 (이전 500ms)
                    Thread.sleep(200)
                } catch (e: Exception) {
                    println("새 emitter 생성 실패: $userId - ${e.message}")
                    return
                }
            }

            // 알림 전송 시도
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data)
                        .reconnectTime(3000))
                println("알림 전송 성공: $userId")
            } catch (e: Exception) {
                println("알림 전송 실패, emitter 제거: $userId, 오류: ${e.message}")
                emitters.remove(userId)
                heartbeatTasks.remove(userId)?.cancel(true)
            }
        } catch (e: Exception) {
            println("알림 전송 중 예외 발생: ${e.message}")
            e.printStackTrace()
        }
    }
}