package com.test.kakaopayTest.domain;

import java.time.LocalDateTime;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(indexes = { @Index(name = "IDX_DISTRIBUTE", unique = false, columnList = "room_id, token") })
public class Distribute {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long _id;

	@Column(name = "room_id", nullable = false, length = 50)
	private String roomId;

	@Column(name = "master_user_id", nullable = false)
	private int masterUserId;

	@Column(columnDefinition = "varchar(3) default '' ", nullable = false)
	private String token;

	private long amount;

	private int count;

	@Column(name = "create_date", nullable = false)
	private LocalDateTime createDate;
}
