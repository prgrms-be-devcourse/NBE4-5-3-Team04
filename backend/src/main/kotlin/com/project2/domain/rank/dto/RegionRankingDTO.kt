package com.project2.domain.rank.dto

import com.project2.domain.place.enums.Region

data class RegionRankingDTO(
        val region: Region,
        val likeCount: Long,
        val scrapCount: Long,
        val postCount: Long
) {
    fun toResponse(): RegionRankingResponseDTO {
        return RegionRankingResponseDTO(
                regionName = region.krRegion,
                region = region,
                likeCount = likeCount,
                scrapCount = scrapCount,
                postCount = postCount
        )
    }
}
