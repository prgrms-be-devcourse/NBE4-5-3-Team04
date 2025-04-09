package com.project2.domain.rank.repository

import com.project2.domain.place.enums.Region
import com.project2.domain.post.entity.Post
import com.project2.domain.rank.dto.PopularPlaceDTO
import com.project2.domain.rank.dto.RegionRankingDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RankingRepository : JpaRepository<Post, Long> {

    @Query(
            """
        SELECT new com.project2.domain.rank.dto.PopularPlaceDTO(
            pl.id, pl.name, pl.region,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT s.id),
            COUNT(p.id)
        )
        FROM Post p
        JOIN p.place pl
        LEFT JOIN p.likes l ON l.post = p
        LEFT JOIN p.scraps s ON s.post = p
        WHERE p.createdDate >= :startDate
          AND (:placeName IS NULL OR pl.name LIKE CONCAT('%', :placeName, '%'))
        GROUP BY pl.id, pl.name, pl.region
        ORDER BY COUNT(DISTINCT l.id) DESC
        """
    )
    fun findPopularPlaces(
            @Param("startDate") startDate: LocalDateTime,
            @Param("placeName") placeName: String?,
            pageable: Pageable
    ): Page<PopularPlaceDTO>

    @Query(
            """
        SELECT new com.project2.domain.rank.dto.RegionRankingDTO(
            pl.region,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT s.id),
            COUNT(p.id)
        )
        FROM Post p
        JOIN p.place pl
        LEFT JOIN p.likes l ON l.post.id = p.id
        LEFT JOIN p.scraps s ON s.post.id = p.id
        WHERE p.createdDate >= :startDate
        GROUP BY pl.region
        ORDER BY COUNT(DISTINCT l.id) DESC
        """
    )
    fun findRegionRankings(
            @Param("startDate") startDate: LocalDateTime,
            pageable: Pageable
    ): Page<RegionRankingDTO>

    @Query(
            """
        SELECT p FROM Post p
        WHERE p.place.region = :region AND p.createdDate >= :startDate
        ORDER BY SIZE(p.likes) DESC
        """
    )
    fun findPostsByRegion(
            @Param("region") region: Region,
            @Param("startDate") startDate: LocalDateTime,
            pageable: Pageable
    ): Page<Post>

    @EntityGraph(attributePaths = ["place", "member", "images", "likes", "scraps", "comments"])
    @Query(
            """
        SELECT p FROM Post p
        WHERE p.place.id = :placeId AND p.createdDate >= :startDate
        ORDER BY SIZE(p.likes) DESC
        """
    )
    fun findPostsByPlace(
            @Param("placeId") placeId: Long,
            @Param("startDate") startDate: LocalDateTime,
            pageable: Pageable
    ): Page<Post>
}
