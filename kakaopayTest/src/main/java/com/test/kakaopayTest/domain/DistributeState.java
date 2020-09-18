package com.test.kakaopayTest.domain;

import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Version;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(indexes = { @Index(name = "FK_distribute_state", unique = false, columnList = "room_id, token")})
public class DistributeState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long _id;

	@Column(name = "room_id", nullable = false, length = 50)
	private String roomId;

	@Column(columnDefinition = "varchar(3) default '' ", nullable = false)
	private String token;

	private long amount;

	@Column(name = "user_id", nullable = false, columnDefinition = "integer default -1")
	private int userId;
	
	@Version
	@Column(name = "VERSION")
	private int version;

}
