package com.test.kakaopayTest.domain;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CommonResponse {
	private String result;
	private Map<String, String> info;
	private Object data;

	public void setMsg(String code, String msg) {
		this.info = new HashMap<String, String>();
		this.info.put("code", code);
		this.info.put("msg", msg);
	}
}
