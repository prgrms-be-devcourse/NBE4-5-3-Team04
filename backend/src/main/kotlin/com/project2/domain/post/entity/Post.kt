package com.project2.domain.post.entity

import com.project2.domain.member.entity.Member
import com.project2.domain.place.entity.Place
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Post() : BaseTime() {경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    lateinit var content: String

    @Column(nullable = false)
    lateinit var title: String

    @ManyToOne
    @JoinColumn(nullable = false)
    lateinit var member: Member

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableSet<PostImage> = mutableSetOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var likes: MutableSet<Likes> = mutableSetOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var scraps: MutableSet<Scrap> = mutableSetOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableSet<Comment> = mutableSetOf()

    @ManyToOne
    @JoinColumn(name = "place_id", referencedColumnName = "id")
    var place: Place? = null

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }
}
