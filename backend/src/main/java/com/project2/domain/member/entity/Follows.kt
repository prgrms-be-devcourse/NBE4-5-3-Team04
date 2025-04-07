package com.project2.domain.member.entity

import com.project2.global.entity.BaseTime
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor


@Entity
@NoArgsConstructor
class Follows : BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    var follower: Member? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    var following: Member? = null

    constructor(id: Long?, follower: Member?, following: Member?) {
        this.id = id
        this.follower = follower
        this.following = following
    }

}
