package com.project2.global.aspect

import com.project2.domain.member.entity.Member
import com.project2.domain.notification.event.NotificationEvent
import com.project2.global.annotation.NotifyEvent
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.stereotype.Component

@Aspect
@Component
class NotifyEventAspect(
        private val eventPublisher: ApplicationEventPublisher
) {
    private val nameDiscoverer = DefaultParameterNameDiscoverer()

    @AfterReturning("@annotation(notifyEvent)", returning = "result")
    fun after(joinPoint: JoinPoint, notifyEvent: NotifyEvent, result: Any?) {
        val method = (joinPoint.signature as MethodSignature).method
        val paramNames = nameDiscoverer.getParameterNames(method) ?: return
        val args = joinPoint.args
        val argMap = paramNames.zip(args).toMap()

        val receiver = argMap[notifyEvent.receiver] as? Member ?: return
        val sender = argMap[notifyEvent.sender] as? Member ?: return
        val relatedId = argMap[notifyEvent.relatedId] as? Long ?: return
        val content = renderTemplate(notifyEvent.content, argMap)

        val event = NotificationEvent(
                receiver = receiver,
                sender = sender,
                type = notifyEvent.type,
                content = content,
                relatedId = relatedId
        )
        eventPublisher.publishEvent(event)
    }

    private fun renderTemplate(template: String, context: Map<String, Any?>): String {
        var rendered = template
        context.forEach { (key, value) ->
            rendered = rendered.replace("{$key}", value.toString())
        }
        return rendered
    }
}