package com.project3.domain.rank.dto

import com.project3.domain.place.enums.Region

data class RegionRankingResponseDTO(
        val regionName: String,
        val region: Region,
        val likeCount: Long,
        val scrapCount: Long,
        val postCount: Long
)
