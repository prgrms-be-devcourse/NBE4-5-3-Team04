package com.project2.domain.rank.dto

import com.project2.domain.place.enums.Region

data class RegionRankingResponseDTO(
        val regionName: String,
        val region: Region,
        val likeCount: Long,
        val scrapCount: Long,
        val postCount: Long
)
