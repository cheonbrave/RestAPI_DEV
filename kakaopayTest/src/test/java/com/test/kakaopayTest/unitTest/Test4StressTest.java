package com.test.kakaopayTest.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.test.kakaopayTest.domain.CommonResponse;
import com.test.kakaopayTest.domain.SetDistributeData;
import com.test.kakaopayTest.repository.DistributeRepository;
import com.test.kakaopayTest.repository.DistributeStateRepository;
import com.test.kakaopayTest.service.DistributeService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Test4StressTest {
	
	@Autowired DistributeService distService;
	@Autowired DistributeRepository distRepo;
	@Autowired DistributeStateRepository distStateRepo;
	
	/*
	 * 시나리오
	 * 4. 스트레스테스트
	 * 		4_1) 뿌리기 / 뿌려진금액 할당받기 100회 반복 (금액 : 1원~200만원, 유저수 : 1명 ~ 100명)
	 */
	
	@Test
	@DisplayName("뿌리기 / 뿌려진금액 할당받기 100회 반복 (금액 : 1원~200만원, 유저수 : 1명 ~ 100명)")
	void 스트레스테스트4_1() throws Exception {
    	
		// 100회 반복
		for(int i=0; i < 100; i++) {
			
			// 뿌리기 셋팅 시작
			int randomCnt = (int)(Math.random() * 100 + 1);

			String roomId = "room" + randomCnt;
			int masterUserId = randomCnt;
			long randomAmount = (long)(Math.random() * 2000000 + 1); // 카카오페이 뿌리기 1회 최고한도 금액
			if(randomAmount < randomCnt) {
				randomAmount += randomCnt;
			}
			
			SetDistributeData reqData = new SetDistributeData();
			reqData.setAmount(randomAmount);
			reqData.setCount(randomCnt); 
			
			String token = distService.setDistribute(roomId, masterUserId, reqData);
			
			assertThat(token.length()).isEqualTo(3);
			// 뿌리기 셋팅 완료
			
			// 뿌려진 돈 전부 할당하기
			int userId;
			long sumAmount = 0;
			CommonResponse cr = null;
			for(int j=0; j < randomCnt; j++) {
				userId = j;
				cr = distService.getDistributedMoney(roomId, userId, token);
				
				/* 분배금액 할당 성공 */
				assertThat(cr.getResult()).isEqualTo("Y");
				
				sumAmount += Long.parseLong((String) cr.getData());
			}
			
			/* 할당받은 총 금액은, 분배금액과 동일해야한다 */
			assertThat(sumAmount).isEqualTo(randomAmount);
		}
	}
	
	
	
}
