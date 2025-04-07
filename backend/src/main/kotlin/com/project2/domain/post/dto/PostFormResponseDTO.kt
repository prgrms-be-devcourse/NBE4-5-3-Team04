package com.project2.domain.post.dto

import com.project2.domain.post.entity.Post

class PostFormResponseDTO(
        post: Post
) {
    val title: String = post.title
    val content: String = post.content
    val placeId: Long = post.place.id!!
    val latitude: Double = post.place.latitude
    val longitude: Double = post.place.longitude
    val placeName: String = post.place.name
    val category: String = post.place.category.krCategory
    val region: String = post.place.region.krRegion
    val images: List<String> = post.images.map { it.imageUrl }.sorted()
}