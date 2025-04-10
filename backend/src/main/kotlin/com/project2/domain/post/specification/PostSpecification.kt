package com.project2.domain.post.specification

import com.project2.domain.place.entity.Place
import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.post.entity.Post
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object PostSpecification {

    @JvmStatic
    fun filterByPlaceAndCategory(
            placeName: String?,
            placeCategory: Category?,
            placeRegion: Region?
    ): Specification<Post> {
        return Specification { root, query, criteriaBuilder ->
            var predicate: Predicate = criteriaBuilder.conjunction()

            if (!placeName.isNullOrEmpty()) {
                val placeJoin: Join<Post, Place> = root.join("place")
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(placeJoin.get("name"), "%$placeName%")
                )
            }

            if (placeCategory != null) {
                val placeJoin: Join<Post, Place> = root.join("place")
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(placeJoin.get<Category>("category"), placeCategory)
                )
            }

            if (placeRegion != null) {
                val placeJoin: Join<Post, Place> = root.join("place")
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(placeJoin.get<Region>("region"), placeRegion)
                )
            }

            predicate
        }
    }
}