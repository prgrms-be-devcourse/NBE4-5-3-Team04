package com.project2.global.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Service
class ImageService(
        @Value("\${custom.file.upload-dir}")
        private val uploadDir: String
) {

    companion object {
        private const val PROFILE_DIR = "profiles/"
    }

    /**
     * URL을 통해 이미지를 다운로드하여 저장하는 메서드
     */
    fun downloadProfileImage(imageUrl: String?, memberId: Long): String {
        if (imageUrl.isNullOrEmpty()) return ""

        val folderPath = "$uploadDir$PROFILE_DIR$memberId/"
        val currentTimestamp = System.currentTimeMillis()
        val savePath = "$folderPath$currentTimestamp.png"

        try {
            val url = URI(imageUrl).toURL()
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.setRequestProperty("Referer", "https://www.naver.com")

            try {
                connection.inputStream.use { inputStream ->
                    deleteExistingFiles(folderPath)

                    val file = File(savePath)
                    file.parentFile.mkdirs()
                    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            } finally {
                connection.disconnect()
            }

            return "/$savePath"
        } catch (e: Exception) {
            throw RuntimeException("이미지 다운로드 실패: ${e.message}", e)
        }
    }

    /**
     * 사용자가 직접 업로드한 이미지를 저장하는 메서드
     */
    fun storeProfileImage(memberId: Long, file: MultipartFile): String {
        try {
            val currentTimestamp = System.currentTimeMillis()
            val folderPath = "$uploadDir$PROFILE_DIR$memberId"
            val folder = File(folderPath)
            if (!folder.exists()) folder.mkdirs()

            deleteExistingFiles(folderPath)

            val fileExtension = getFileExtension(file.originalFilename)
            val newFileName = "$currentTimestamp.$fileExtension"
            val filePath = "$folderPath/$newFileName"

            val path = Path.of(filePath)
            Files.copy(file.inputStream, path, StandardCopyOption.REPLACE_EXISTING)

            return "/$filePath"
        } catch (e: IOException) {
            throw RuntimeException("파일 저장 실패: ${e.message}", e)
        }
    }

    /**
     * 사용자의 프로필 폴더 내 모든 파일 삭제
     */
    private fun deleteExistingFiles(folderPath: String) {
        val folder = File(folderPath)
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (!file.delete()) {
                    throw RuntimeException("기존 파일 삭제 실패: ${file.absolutePath}")
                }
            }
        }
    }

    /**
     * 파일 확장자 추출
     */
    private fun getFileExtension(fileName: String?): String {
        if (fileName.isNullOrEmpty()) return "png"
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex == -1) "png" else fileName.substring(lastDotIndex + 1)
    }
}