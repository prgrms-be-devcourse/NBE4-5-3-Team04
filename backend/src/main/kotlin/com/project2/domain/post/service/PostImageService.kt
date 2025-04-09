package com.project2.domain.post.service

import com.project2.domain.post.entity.Post
import com.project2.domain.post.entity.PostImage
import com.project2.domain.post.repository.PostImageRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
class PostImageService(
        private val postImageRepository: PostImageRepository,
        @Value("\${custom.file.upload-dir}") private val uploadDir: String
) {

    private val uploadPostImageDir: String = "$uploadDir/post-images"

    /**
     * 게시글 이미지 저장
     */
    @Throws(IOException::class)
    fun saveImages(post: Post, images: List<MultipartFile>) {
        val postDir = File(uploadPostImageDir, post.id.toString())
        if (!postDir.exists()) {
            postDir.mkdirs()
        }

        images.forEachIndexed { i, image ->
            val originalFilename = image.originalFilename ?: ""
            val extension = if ("." in originalFilename) originalFilename.substringAfterLast(".", "") else ""

            val filename = "$i.$extension"
            val filePath = Paths.get(uploadPostImageDir, post.id.toString(), filename)
            Files.copy(image.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            val imageUrl = "/${Paths.get(uploadDir, "post-images", post.id.toString(), filename)}"
                    .replace("\\", "/")

            val postImage = PostImage().apply {
                this.post = post
                this.imageUrl = imageUrl
            }
            postImageRepository.save(postImage)
        }
    }

    /**
     * 게시글 이미지 업데이트
     */
    @Throws(IOException::class)
    fun updateImages(post: Post, newImages: List<MultipartFile>?) {
        post.images.clear()

        val folder = File("$uploadPostImageDir/${post.id}")
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { it.delete() }
        }

        newImages?.let { saveImages(post, it) }
    }
}
