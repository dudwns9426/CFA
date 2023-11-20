package com.project.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long user_id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column
	private String locale;

	@OneToOne(mappedBy = "user")
	private Token token;

	@Builder
	public User(String email, String locale) {
		this.email = email;
		this.locale = locale;
	}

	public User update(String email, String locale) {
		this.email = email;
		this.locale = locale;

		return this;
	}

}
