package com.project3.domain.post.mapper

import com.project3.domain.member.entity.Member
import com.project3.domain.post.entity.Likes
import com.project3.domain.post.entity.Post
import com.project3.domain.post.entity.Scrap
import org.springframework.stereotype.Component

@Component
class ToggleMapper {

    fun toLikes(userId: Long, post: Post): Likes {
        val member = Member().apply { id = userId }
        return Likes().apply {
            this.post = post
            this.member = member
        }
    }

    fun toScrap(userId: Long, post: Post): Scrap {
        val member = Member().apply { id = userId }
        return Scrap(post = post, member = member)
    }
}