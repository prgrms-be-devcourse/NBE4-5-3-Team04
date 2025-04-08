package com.project2.domain.place.entity;

import java.util.ArrayList;
import java.util.List;

import com.project2.domain.place.enums.Category;
import com.project2.domain.place.enums.Region;
import com.project2.domain.post.entity.Post;
import com.project2.global.entity.BaseTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Place extends BaseTime {
	@Id
	@Column(name = "id")
	public Long id;

	@Column(nullable = false)
	public String name;

	@Column(nullable = false)
	public Double latitude;

	@Column(nullable = false)
	public Double longitude;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public Region region;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public Category category;

	@OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	public List<Post> posts = new ArrayList<>();

	public String getKrCategory() {
		return category.getKrCategory();
	}
}