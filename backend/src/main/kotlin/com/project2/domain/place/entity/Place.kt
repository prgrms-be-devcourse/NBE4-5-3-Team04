package com.project2.domain.place.entity

import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.post.entity.Post
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Place(
    @Id
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var region: Region = Region.ETC,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: Category = Category.ETC,

    @OneToMany(mappedBy = "place", cascade = [CascadeType.ALL], orphanRemoval = true)
    var posts: MutableList<Post> = mutableListOf()
) : BaseTime() {

    val krCategory: String
        get() = category.krCategory
}
