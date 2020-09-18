package com.test.kakaopayTest.unitTest;

import static org.hamcrest.CoreMatchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.kakaopayTest.consts.Returns;
import com.test.kakaopayTest.domain.SetDistributeData;
import com.test.kakaopayTest.repository.DistributeRepository;
import com.test.kakaopayTest.repository.DistributeStateRepository;
import com.test.kakaopayTest.service.DistributeService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Test1SetDistribute {

	static final String ROOM_ID = "X-ROOM-ID";
	static final String USER_ID = "X-USER-ID";
	
	@Autowired DistributeService distService;
	@Autowired DistributeRepository distRepo;
	@Autowired DistributeStateRepository distStateRepo;
	
	 @Autowired private MockMvc mvc;
	 
	 @Autowired private ObjectMapper mapper;
	
	/*
	 * 시나리오
	 * 1. 뿌리기신청
	 * 		1_1) X-ROOM-ID가 공백일때 -> 실패
	 * 		1_2) X-USER-ID가 0보다 작을때 -> 실패
	 * 		1_3) 수신된 JSON 데이터에서 금액이 0 이하일때 -> 실패
	 * 		1_4) 수신된 JSON 데이터에서 분배인원이  0 이하일때 -> 실패
	 * 		1_5) 수신된 금액이 분배인원보다 적을경우 -> 실패 (최소 1원 보장을 위함)
	 *		1_6) 모든 파라미터가 정상일때 -> 성공 
	 *
	 */
	
    @Test
	@DisplayName("뿌리기신청 - X-ROOM-ID 공백시 실패")
	void 뿌리기신청1_1() throws Exception {
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(10000);
		reqData.setCount(3);
		
		mvc.perform(
				post("/remittance/distribute")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "1")
				.header(ROOM_ID, "")
				.content(mapper.writeValueAsString(reqData)))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
    
    @Test
	@DisplayName("뿌리기신청 - X-USER-ID가 0보다 작을때 실패")
	void 뿌리기신청1_2() throws Exception {
    	
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(10000);
		reqData.setCount(3);
		
		mvc.perform(
				post("/remittance/distribute")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "-1")
				.header(ROOM_ID, "room1")
				.content(mapper.writeValueAsString(reqData)))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
    
    @Test
	@DisplayName("뿌리기신청 - 수신된 JSON 데이터에서 금액이 0 이하일때 실패")
	void 뿌리기신청1_3() throws Exception {
    	
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(0);
		reqData.setCount(3);
		
		mvc.perform(
				post("/remittance/distribute")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "1")
				.header(ROOM_ID, "room1")
				.content(mapper.writeValueAsString(reqData)))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
    
    @Test
	@DisplayName("뿌리기신청 - 수신된 JSON 데이터에서 분배인원이  1 이하일때 실패")
	void 뿌리기신청1_4() throws Exception {
    	
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(10000);
		reqData.setCount(0);
		
		mvc.perform(
				post("/remittance/distribute")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "1")
				.header(ROOM_ID, "room1")
				.content(mapper.writeValueAsString(reqData)))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
    
    @Test
	@DisplayName("뿌리기신청 -수신된 금액이 분배인원보다 적을경우 실패 (최소 1원 보장을 위함)")
	void 뿌리기신청1_5() throws Exception {
    	
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(50);
		reqData.setCount(51);
		
		mvc.perform(
				post("/remittance/distribute")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "1")
				.header(ROOM_ID, "room1")
				.content(mapper.writeValueAsString(reqData)))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
    
    @Test
	@DisplayName("뿌리기신청 - 모든 파라미터가 정상일때 성공")
	void 뿌리기신청1_6() throws Exception {
    	
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(10000);
		reqData.setCount(3);
		
		mvc.perform(
				post("/remittance/distribute")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "1")
				.header(ROOM_ID, "room1")
				.content(mapper.writeValueAsString(reqData)))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.SUCCESS)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
}
