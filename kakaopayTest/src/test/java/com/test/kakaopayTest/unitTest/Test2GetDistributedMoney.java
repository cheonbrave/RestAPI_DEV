package com.test.kakaopayTest.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.test.kakaopayTest.consts.Returns;
import com.test.kakaopayTest.domain.Distribute;
import com.test.kakaopayTest.domain.SetDistributeData;
import com.test.kakaopayTest.repository.DistributeRepository;
import com.test.kakaopayTest.repository.DistributeStateRepository;
import com.test.kakaopayTest.service.DistributeService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Test2GetDistributedMoney {

	static final String ROOM_ID = "X-ROOM-ID";
	static final String USER_ID = "X-USER-ID";
	
	@Autowired DistributeService distService;
	@Autowired DistributeRepository distRepo;
	@Autowired DistributeStateRepository distStateRepo;
	
	 @Autowired private MockMvc mvc;
	 
	/*
	 * 시나리오
	 * 2. 뿌린돈 받기
	 * 		2_1) 토큰일치, 같은방, 뿌린사람이 받으려고 할때 - 실패
	 * 		2_2) 토큰불일치, 같은방, 방구성원이 받으려고 할때 - 실패
	 * 		2_3) 토큰일치, 다른방, 방구성원이 받으려고 할때 - 실패
	 * 		2_4) 
	 * 			- 토큰일치, 같은방, 방구성원이 받으려고 할때 - 성공
	 *      	- 토큰일치, 같은방, 이미 받은인원이 받으려고 할때 - 실패
	 *     2_5) 토큰일치, 같은방, 뿌리기가 더이상 없을때 - 실패
	 *     2_6) 토큰일치, 같은방, 10분 경과했을때 - 실패 
	 */
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 뿌린사람이 받으려고 할때 - 실패")
	void 뿌린돈받기2_1() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				put("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, String.valueOf(masterUserId))
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_CANNOT_RCV)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰불일치, 같은방, 방구성원이 받으려고 할때 - 실패")
	void 뿌린돈받기2_2() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				put("/remittance/distribute/abcd")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "2")
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 다른방, 방구성원이 받으려고 할때 - 실패")
	void 뿌린돈받기2_3() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				put("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "2")
				.header(ROOM_ID, "room2"))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_REQUEST)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	 
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 방구성원이 받으려고 할때 - 성공 이후 또 받으려고 할때 - 실패")
	void 뿌린돈받기2_4() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				put("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "2")
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.SUCCESS)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    	
    	mvc.perform(
				put("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "2")
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_ALREADY_RCV)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 뿌리기가 더이상 없을때 - 실패")
	void 뿌린돈받기2_5() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
		for(int i=2; i < 5; i++) {
			mvc.perform(
					put("/remittance/distribute/" + token)
					.contentType(MediaType.APPLICATION_JSON)
	                .accept(MediaType.APPLICATION_JSON)
					.header(USER_ID, String.valueOf(i))
					.header(ROOM_ID, roomId))
			.andDo(print())	
			.andExpect(status().isOk())
	        .andExpect(jsonPath("$.info.code", is(Returns.SUCCESS)))
	        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
		}
		
    	mvc.perform(
				put("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "5")
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_END)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 10분 경과했을때 - 실패")
	void 뿌린돈받기2_6() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		assertThat(token.length()).isEqualTo(3);
		
		/* 방금 생성된 뿌리기의 create_date를 10분 이전으로 설정함*/
		Distribute di = distRepo.findByRoomIdAndToken(roomId, token).orElse(null);
		if(di != null) {
			LocalDateTime lt = LocalDateTime.now().minusMinutes(10);
			di.setCreateDate(lt);
			distRepo.save(di);
		}
    	
		mvc.perform(
				put("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "2")
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_TIMEOVER)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
}