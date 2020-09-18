package com.test.kakaopayTest.domain;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InqDistributeData {
	private String createDate;
	private long amount;
	private long distributedAmount;
	private List<Map<String, String>> distStateList;
}
