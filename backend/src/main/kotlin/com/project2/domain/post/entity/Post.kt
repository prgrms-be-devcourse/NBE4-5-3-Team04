package com.project2.domain.post.entity

import com.project2.domain.member.entity.Member
import com.project2.domain.place.entity.Place
import com.project2.global.entity.BaseTime
import jakarta.persistence.*

@Entity
class Post(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(nullable = false, columnDefinition = "TEXT")
        var content: String,

        var title: String,

        @ManyToOne
        @JoinColumn(nullable = false)
        var member: Member,

        @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
        var images: MutableSet<PostImage> = mutableSetOf(),

        @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
        var likes: MutableSet<Likes> = mutableSetOf(),

        @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
        var scraps: MutableSet<Scrap> = mutableSetOf(),

        @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
        var comments: MutableSet<Comment> = mutableSetOf(),

        @ManyToOne
        @JoinColumn(name = "place_id", referencedColumnName = "id")
        var place: Place? = null
) : BaseTime() {

    companion object {
        fun builder() = PostBuilder()
    }

    class PostBuilder {
        private var id: Long? = null
        private var content: String = ""
        private var title: String = ""
        private lateinit var member: Member
        private var images: MutableSet<PostImage> = mutableSetOf()
        private var likes: MutableSet<Likes> = mutableSetOf()
        private var scraps: MutableSet<Scrap> = mutableSetOf()
        private var comments: MutableSet<Comment> = mutableSetOf()
        private var place: Place? = null

        fun id(id: Long?) = apply { this.id = id }
        fun content(content: String) = apply { this.content = content }
        fun title(title: String) = apply { this.title = title }
        fun member(member: Member) = apply { this.member = member }
        fun images(images: MutableSet<PostImage>) = apply { this.images = images }
        fun likes(likes: MutableSet<Likes>) = apply { this.likes = likes }
        fun scraps(scraps: MutableSet<Scrap>) = apply { this.scraps = scraps }
        fun comments(comments: MutableSet<Comment>) = apply { this.comments = comments }
        fun place(place: Place?) = apply { this.place = place }

        fun build() = Post(
                id = id,
                content = content,
                title = title,
                member = member,
                images = images,
                likes = likes,
                scraps = scraps,
                comments = comments,
                place = place
        )
    }

    constructor() : this(
            id = null,
            content = "",
            title = "",
            member = Member(),
            images = mutableSetOf(),
            likes = mutableSetOf(),
            scraps = mutableSetOf(),
            comments = mutableSetOf(),
            place = null
    )

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }
}
