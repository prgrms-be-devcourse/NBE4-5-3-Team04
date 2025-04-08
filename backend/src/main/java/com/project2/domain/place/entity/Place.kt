package com.project2.domain.place.entity

import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.post.entity.Post
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Place(
    @Id
    @Column(name = "id")
    var id: Long = 0L,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var latitude: Double = 0.0,

    @Column(nullable = false)
    var longitude: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var region: Region = Region.ETC,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: Category = Category.ETC,

    @OneToMany(mappedBy = "place", cascade = [CascadeType.ALL], orphanRemoval = true)
    var posts: MutableList<Post> = mutableListOf()
) : BaseTime() {

    companion object {
        @JvmStatic
        fun builder() = PlaceBuilder()
    }

    class PlaceBuilder {
        private var id: Long = 0L
        private var name: String = ""
        private var latitude: Double = 0.0
        private var longitude: Double = 0.0
        private var region: Region = Region.ETC
        private var category: Category = Category.ETC
        private var posts: MutableList<Post> = mutableListOf()

        fun id(id: Long) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun latitude(latitude: Double) = apply { this.latitude = latitude }
        fun longitude(longitude: Double) = apply { this.longitude = longitude }
        fun region(region: Region) = apply { this.region = region }
        fun category(category: Category) = apply { this.category = category }
        fun posts(posts: MutableList<Post>) = apply { this.posts = posts }

        fun build() = Place(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            region = region,
            category = category,
            posts = posts
        )
    }

    val krCategory: String
        get() = category.krCategory
}