package com.project3.domain.post.repository

import com.project3.domain.member.entity.Member
import com.project3.domain.post.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PostRepository : JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    // 전체 게시글 조회
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    override fun findAll(spec: Specification<Post>?, pageable: Pageable): Page<Post>

    // 사용자가 좋아요 누른 게시글 조회
    @Query("""
        SELECT p FROM Post p
        JOIN p.likes l
        WHERE l.member.id = :memberId
    """)
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    fun findLikedPosts(@Param("memberId") memberId: Long, pageable: Pageable): Page<Post>

    // 사용자가 스크랩한 게시글 조회
    @Query("""
        SELECT p FROM Post p
        JOIN p.scraps s
        WHERE s.member.id = :memberId
    """)
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    fun findScrappedPosts(@Param("memberId") memberId: Long, pageable: Pageable): Page<Post>

    // 팔로우하는 사람들의 게시글 조회
    @Query("""
        SELECT p FROM Post p
        WHERE p.member.id IN (
            SELECT f.following.id FROM Follows f WHERE f.follower.id = :memberId
        )
    """)
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    fun findFollowingPosts(@Param("memberId") memberId: Long, pageable: Pageable): Page<Post>

    // 특정 사용자의 게시글 조회
    @Query("""
        SELECT p FROM Post p
        WHERE p.member.id = :targetMemberId
    """)
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    fun findPostsByMember(@Param("targetMemberId") targetMemberId: Long, pageable: Pageable): Page<Post>

    // 특정 장소내의 게시글 조회
    @Query("""
        SELECT p FROM Post p
        JOIN p.place pl
        WHERE pl.id = :targetPlaceId
    """)
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    fun findPostsByPlace(@Param("targetPlaceId") targetPlaceId: Long, pageable: Pageable): Page<Post>

    // 단일 게시글 상세 조회
    @EntityGraph(attributePaths = ["place", "member", "likes", "scraps", "comments", "images"])
    override fun findById(id: Long): Optional<Post>

    @Query("""
        SELECT p from Post p WHERE p.id = :id
    """)
    @EntityGraph(attributePaths = ["place", "images"])
    fun findByIdForEdit(id: Long): Post?

    fun countByMember(actor: Member): Long
}
