package com.project2.domain.member.repository

import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FollowRepository : JpaRepository<Follows?, Long?> {
    fun findByFollowerAndFollowing(follower: Member?, following: Member?): Optional<Follows?>?

    fun findByFollowing(following: Member?, pageable: Pageable?): Page<Follows?>?

    fun findByFollowing(following: Member?): List<Follows?>?

    fun findByFollower(follower: Member?): List<Follows?>?

    fun countByFollower(follower: Member?): Long

    fun countByFollowing(member: Member?): Long

    fun existsByFollowerAndFollowing(follower: Member?, following: Member?): Boolean

    fun deleteByFollowerAndFollowing(follower: Member?, following: Member?)
}