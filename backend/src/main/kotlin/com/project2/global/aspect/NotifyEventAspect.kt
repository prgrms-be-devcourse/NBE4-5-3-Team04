package com.project2.global.aspect

import com.project2.domain.member.entity.Member
import com.project2.domain.notification.event.NotificationEvent
import com.project2.global.annotation.NotifyEvent
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.expression.MapAccessor
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

@Aspect
@Component
class NotifyEventAspect(
        private val eventPublisher: ApplicationEventPublisher
) {
    private val nameDiscoverer = DefaultParameterNameDiscoverer()
    private val parser = SpelExpressionParser()

    @AfterReturning("@annotation(notifyEvent)", returning = "result")
    fun after(joinPoint: JoinPoint, notifyEvent: NotifyEvent, result: Any?) {
        try {
            val method = (joinPoint.signature as MethodSignature).method
            val paramNames = nameDiscoverer.getParameterNames(method) ?: return
            val args = joinPoint.args
            val argMap = paramNames.zip(args).toMap().toMutableMap()

            // 결과값도 컨텍스트에 추가
            if (result != null) {
                argMap["result"] = result
            }

            // SpEL을 사용하여 표현식 평가
            val context = StandardEvaluationContext(argMap)
            context.addPropertyAccessor(MapAccessor())

            // 각 파라미터를 변수로 등록
            argMap.forEach { (key, value) ->
                context.setVariable(key, value)
            }

            val receiver = parser.parseExpression(notifyEvent.receiver).getValue(context, Member::class.java) ?: return
            val sender = parser.parseExpression(notifyEvent.sender).getValue(context, Member::class.java) ?: return
            val relatedIdStr = notifyEvent.relatedId
            val relatedId = parser.parseExpression(relatedIdStr).getValue(context, Long::class.java) ?: return
            val contentExpr = notifyEvent.content
            val content = parser.parseExpression(contentExpr).getValue(context, String::class.java) ?: return

            val event = NotificationEvent(
                    receiver = receiver,
                    sender = sender,
                    type = notifyEvent.type,
                    content = content,
                    relatedId = relatedId
            )
            eventPublisher.publishEvent(event)
        } catch (e: Exception) {
            // 예외 발생 시 로깅하고 계속 진행
            println("알림 생성 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
    }
}
