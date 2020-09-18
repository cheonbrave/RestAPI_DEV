package com.test.kakaopayTest.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.test.kakaopayTest.domain.DistributeState;
import com.test.kakaopayTest.domain.SetDistributeData;
import com.test.kakaopayTest.repository.DistributeRepository;
import com.test.kakaopayTest.repository.DistributeStateRepository;
import com.test.kakaopayTest.service.DistributeService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Test5StressTestMultiThread {
	
	@Autowired DistributeService distService;
	@Autowired DistributeRepository distRepo;
	@Autowired DistributeStateRepository distStateRepo;
	
	/*
	 * 시나리오
	 * 5. 동시성제어문제 재현
	 * 		5_1) 동일한 데이터를 얻어서 서로 업데이트 -> 락 발생 -> 재시도
	 */
	
	@Test
	@DisplayName("동시성제어 : 동일한 데이터를 얻어서 서로 업데이트")
	void 스트레스테스트5_1() throws Exception {
    	
		/* 뿌리기 */
		String roomId = "room1";
		
		int count = 2;
		int masterUserId = count;
		long amount = 100;
		
		SetDistributeData reqData = new SetDistributeData();
		reqData.setAmount(amount);
		reqData.setCount(count); 
		
		String token = distService.setDistribute(roomId, masterUserId, reqData);
		assertThat(token.length()).isEqualTo(3);
		/* 뿌리기 셋팅 완료 */
		
		int userId1 = 111;
		int userId2 = 222;
		
		List<DistributeState> dsiList = null;
		DistributeState dsi1 = null;
		DistributeState dsi2 = null;
		
		/* 뿌리기 금액 할당 */
		dsiList = distStateRepo.findByRoomIdAndTokenAndUserId(roomId, token, -1);
		dsi1 = dsiList.get(0);
		dsi2 = dsiList.get(0);
			
		/* 할당받을 userId로 DB UPDATE */
		dsi1.setUserId(userId1);
		distStateRepo.save(dsi1);
		
		try {
			dsi2.setUserId(userId2);
			distStateRepo.save(dsi2);
		}catch (Exception e) {
			/* 동시성문제로 락에 걸린 실패건에 대해서 새로운 분배금액을 할당받아서 업데이트 수행 */
			dsiList = distStateRepo.findByRoomIdAndTokenAndUserId(roomId, token, -1);
			dsi2 = dsiList.get(0);
			dsi2.setUserId(userId2);
			distStateRepo.save(dsi2);
		}
	}
		
	
	
}
