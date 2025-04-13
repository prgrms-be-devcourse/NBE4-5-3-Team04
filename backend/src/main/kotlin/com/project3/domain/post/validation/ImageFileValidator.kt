package com.project3.domain.post.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.web.multipart.MultipartFile

class ImageFileValidator : ConstraintValidator<ImageFile, List<MultipartFile>> {

    companion object {
        private val ALLOWED_MIME_TYPES = listOf("image/jpeg", "image/png", "image/gif")
    }

    override fun isValid(
            files: List<MultipartFile>?,
            context: ConstraintValidatorContext
    ): Boolean {
        if (files.isNullOrEmpty()) {
            return true // 이미지 업로드가 필수 사항이 아니라면 허용
        }

        return files.all { it.contentType in ALLOWED_MIME_TYPES }
    }
}