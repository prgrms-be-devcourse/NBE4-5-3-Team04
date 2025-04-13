package com.project3.global.exception

import com.project3.global.dto.RsData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Void>> {
        val message = e.bindingResult.fieldErrors
                .joinToString("\n") {
                    "${it.field} : ${it.code} : ${it.defaultMessage}"
                }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        RsData(
                                HttpStatus.BAD_REQUEST.value().toString(),
                                message
                        )
                )
    }

    @ExceptionHandler(ServiceException::class)
    @ResponseStatus
    fun handleServiceException(ex: ServiceException): ResponseEntity<RsData<Void>> {
        return ResponseEntity
                .status(ex.statusCode)
                .body(
                        RsData(
                                ex.code,
                                ex.msg
                        )
                )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    fun handleMaxSizeException(ex: MaxUploadSizeExceededException): ResponseEntity<RsData<Void>> {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(
                        RsData(
                                HttpStatus.PAYLOAD_TOO_LARGE.value().toString(),
                                "파일 크기가 너무 큽니다. 각 파일은 최대 10MB, 총 50MB까지 업로드 가능합니다."
                        )
                )
    }
}