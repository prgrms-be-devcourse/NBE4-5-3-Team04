package com.project3.global.app

import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class PageableConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<org.springframework.web.method.support.HandlerMethodArgumentResolver>) {
        val resolver = PageableHandlerMethodArgumentResolver()
        resolver.setFallbackPageable(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdDate")))) // 기본값 설정
        resolvers.add(resolver)
    }
}
