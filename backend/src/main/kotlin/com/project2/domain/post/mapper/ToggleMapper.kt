package com.project2.domain.post.mapper

import com.project2.domain.member.entity.Member
import com.project2.domain.post.entity.Likes
import com.project2.domain.post.entity.Post
import com.project2.domain.post.entity.Scrap
import org.springframework.stereotype.Component

@Component
class ToggleMapper {

    fun toLikes(userId: Long, postId: Long): Likes {
        val post = Post().apply { id = postId }
        val member = Member().apply { id = userId }
        return Likes().apply {
            this.post = post
            this.member = member
        }
    }

    // Scrap 관련 코드들은 해당 기능 변환 때 다시 수정 예정
    fun toScrap(userId: Long, post: Post): Scrap {
        val member = Member().apply { id = userId }
        return Scrap().apply {
            this.post = post
            this.member = member
        }
    }
}