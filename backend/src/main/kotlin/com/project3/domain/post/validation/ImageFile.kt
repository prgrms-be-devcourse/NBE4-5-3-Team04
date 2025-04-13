package com.project3.domain.post.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ImageFileValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ImageFile(
        val message: String = "Only image files are allowed (jpg, jpeg, png, gif, webp)",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)