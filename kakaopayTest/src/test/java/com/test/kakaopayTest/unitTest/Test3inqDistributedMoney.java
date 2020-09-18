package com.test.kakaopayTest.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class Test3inqDistributedMoney {

	static final String ROOM_ID = "X-ROOM-ID";
	static final String USER_ID = "X-USER-ID";
	
	@Autowired DistributeService distService;
	@Autowired DistributeRepository distRepo;
	@Autowired DistributeStateRepository distStateRepo;
	
	 @Autowired private MockMvc mvc;
	 
	/*
	 * 시나리오
	 * 3. 뿌린돈 조회
	 * 		3_1) 토큰일치, 같은방, 뿌린사람 조회 - 성공
	 * 		3_2) 토큰불일치, 같은방, 뿌린사람 조회 - 실패
	 * 		3_3) 토큰일치, 같은방, 다른사람 조회 - 실패
	 *     3_4) 토큰일치, 같은방, 뿌린사람, 7일경과 - 실패 
	 */
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 뿌린사람 조회 - 성공")
	void 뿌린돈조회3_1() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				get("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, String.valueOf(masterUserId))
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.SUCCESS)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰불일치, 같은방, 뿌린사람 조회 - 실패")
	void 뿌린돈조회3_2() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				get("/remittance/distribute/abcd")
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, String.valueOf(masterUserId))
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_INVALID_PARAM)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 다른사람 조회 - 실패")
	void 뿌린돈조회3_3() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		
		assertThat(token.length()).isEqualTo(3);
    	
    	mvc.perform(
				get("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, "2")
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_ONLY_MASTER)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@DisplayName("뿌린돈받기 - 토큰일치, 같은방, 뿌린사람 7일경과 조회 - 실패")
	void 뿌린돈조회3_4() throws Exception {
		
		SetDistributeData sData = new SetDistributeData();
    	sData.setAmount(10000);
    	sData.setCount(3);
    	String roomId = "room1";
    	int masterUserId = 1;
    	
		String token = distService.setDistribute(roomId, masterUserId, sData);
		assertThat(token.length()).isEqualTo(3);
		
		Distribute di = distRepo.findByRoomIdAndToken(roomId, token).orElse(null);
		if(di != null) {
			LocalDateTime lt = LocalDateTime.now().minusDays(7);
			di.setCreateDate(lt);
			distRepo.save(di);
		}
		
    	mvc.perform(
				get("/remittance/distribute/" + token)
				.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
				.header(USER_ID, String.valueOf(masterUserId))
				.header(ROOM_ID, roomId))
		.andDo(print())	
		.andExpect(status().isOk())
        .andExpect(jsonPath("$.info.code", is(Returns.ERR_DAYOVER)))
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	
}