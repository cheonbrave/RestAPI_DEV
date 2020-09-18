package com.test.kakaopayTest.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.test.kakaopayTest.consts.Returns;
import com.test.kakaopayTest.domain.CommonResponse;
import com.test.kakaopayTest.domain.Distribute;
import com.test.kakaopayTest.domain.DistributeState;
import com.test.kakaopayTest.domain.InqDistributeData;
import com.test.kakaopayTest.domain.SetDistributeData;
import com.test.kakaopayTest.repository.DistributeRepository;
import com.test.kakaopayTest.repository.DistributeStateRepository;

@Transactional
public class DistributeService {

	private DistributeRepository distRepo;
	private DistributeStateRepository distStateRepo;

	public DistributeService(DistributeRepository distRepo, DistributeStateRepository distStateRepo) {
		this.distRepo = distRepo;
		this.distStateRepo = distStateRepo;
	}

	/**
	 * 랜덤문자열 생성을 위한 62개 캐릭터 리스트 생성
	 * 
	 * @param strList
	 */
	public ArrayList<String> makeCharList() {
		ArrayList<String> strList = new ArrayList<>();
		
		for (int i = 48; i < 123; i++) {

			/* 48 ~ 122 까지 증가하면서 특수문자 영역을 제외하고 리스트 생성 (숫자, 알파벳 소문자, 알파벳 대문자) */

			if (i >= 58 && i <= 64)
				continue;

			if (i >= 91 && i <= 96)
				continue;

			strList.add(String.valueOf((char) i));
		}
		
		return strList;
	}
	
	/**
	 * token 생성
	 * @param token
	 * @param strList
	 * @param roomId
	 */
	public String makeToken(ArrayList<String> strList, String roomId) {
		
		int loopCnt = 0;
		String token = "";
		Distribute di = null;
		
		while (true) {
			/* 토큰 생성 문자열 3개, 예측불가능한 랜덤 조합 조합 범위 : 아스키코드 33(!) ~ 126(~) */
			for (int i = 0; i < 3; i++) {
				token += strList.get((int) (Math.random() * 62));
			}

			/* 생성한 token이 roomId내에 존재한적 있는지 조회 */
			di = distRepo.findByRoomIdAndToken(roomId, token).orElse(null);

			if (di != null) {
				// 동일한 토큰이 존재하면 토큰 다시생성
				if (loopCnt > 10000) {
					// while loop는 10000번으로 제한
					token = "";
					break;
				} else {
					loopCnt++;
					continue;
				}
			} else {
				break;
			}
		}
		return token;
	}

	/**
	 * 분배금액 할당
	 * @param count
	 * @param amount
	 * @param roomId
	 * @param token
	 * @return
	 */
	public int distMoney(int count, long amount, String roomId, String token) {
		int remainCount;
		long maxAmount;
		long distAmount;
		
		DistributeState dsi = null;
		ArrayList<DistributeState> amtList = new ArrayList<>();
		
		remainCount = count;
		for (int i = 0; i < count; i++) {	
			
			/* 0원을 받는 사람이 없도록, 최소 1원을 보장 */
			maxAmount = amount - remainCount;
			remainCount--;
			             
			if (i == count - 1){
				/* 마지막 사람에게는 남은 잔액을 모두 할당 */
				distAmount = amount;
			} else {
				/* maxAmount의 금액 범위내에서 랜덤하게 분배금액 산정 */
				distAmount = (long) (Math.random() * maxAmount) + 1;
				amount -= distAmount;
			}
			
			dsi = new DistributeState();
			dsi.setRoomId(roomId);
			dsi.setToken(token);
			dsi.setAmount(distAmount);
			dsi.setUserId(-1);
			amtList.add(dsi);
			
		}
		
		/* 상대적으로 높은 금액이 초반에 몰리는 경향이 있으므로 셔플을 수행하여 고르게 분포를 유도 */
		Collections.shuffle(amtList);

		/* 금액 분배내역 DB 저장 */
		amtList = (ArrayList<DistributeState>) distStateRepo.saveAll(amtList);
		
		return amtList.size();
	}
	/**
	 * 토큰발행 및 뿌릴 금액을 인원수에 맞게 분배하여 저장
	 * 
	 * @param amount
	 * @param count
	 * @return
	 */
	public String setDistribute(String roomId, int masterUserId, SetDistributeData reqData) {

		int count = reqData.getCount();
		long amount = reqData.getAmount();
		String token = "";

		Distribute di = null;
		
		
		ArrayList<String> strList = new ArrayList<>();		
		
		/* 금액이 인원수보다 적을경우 최소 1원을 보장할 수 없으므로 토큰을 발행하지 않는다 */
		if(amount < count) {
			token = "";
			return token;
		}
		
		/* 랜덤문자열 생성을 위한 캐릭터 리스트 생성 */
		strList = this.makeCharList();
		
		/* token 생성 */
		token = this.makeToken(strList, roomId);
		
		/* 뿌리기정보 저장 */
		di = new Distribute();
		di.setAmount(reqData.getAmount());
		di.setCount(reqData.getCount());
		di.setMasterUserId(masterUserId);
		di.setRoomId(roomId);
		di.setToken(token);
		di.setCreateDate(LocalDateTime.now());
		di = distRepo.save(di);
		
		if (token.length() == 3 && di != null) {
			
			/* 분배인원수만큼 금액 할당 */
			if(this.distMoney(count, amount, roomId, token) <= 0) {
				
				/* 금액할당 정보를 DB에 정상적으로 저장하지 못했을 경우 실패로 간주하여 토큰을 발행하지 않음 */
				token = "";
			}
		}
		return token;
	}

	/**
	 * 뿌리기 금액을 가져갈수 있는 유저인지 확인
	 * 
	 * @param roomId
	 * @param userId
	 * @param token
	 * @return
	 */
	public CommonResponse userValidate(String roomId, int userId, String token) {
		Distribute di = null;
		List<DistributeState> dsiList = null;
		LocalDateTime lt = LocalDateTime.now().minusMinutes(10); // 현시간으로부터 10분전
		CommonResponse cr = new CommonResponse();

		di = distRepo.findByRoomIdAndToken(roomId, token).orElse(null);
		if (di == null) {
			/* token이 유효하지 않거나, roomId가 다를경우(즉, 다른방일경우) */
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_REQUEST, Returns.ERR_INVALID_REQUEST_MSG);
			return cr;
		} else {
			if (lt.isAfter(di.getCreateDate())) {
				cr.setResult("N");
				cr.setMsg(Returns.ERR_TIMEOVER, Returns.ERR_TIMEOVER_MSG);
				return cr;
			} else if (di.getMasterUserId() == userId) {
				cr.setResult("N");
				cr.setMsg(Returns.ERR_CANNOT_RCV, Returns.ERR_CANNOT_RCV_MSG);
				return cr;
			}
		}

		// 이미 받은 유저인지 체크
		dsiList = distStateRepo.findByRoomIdAndTokenAndUserId(roomId, token, userId);
		if (dsiList != null && dsiList.size() > 0) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_ALREADY_RCV, Returns.ERR_ALREADY_RCV_MSG);
			return cr;
		}

		cr.setResult("Y");

		return cr;
	}

	/**
	 * 분배된 금액중 하나를 선택하여 특정 유저에게 할당
	 * 
	 * @param roomId
	 * @param userId
	 * @param token
	 * @return
	 */
	public CommonResponse getDistributedMoney(String roomId, int userId, String token) throws Exception {
		long amount = -1L;
		List<DistributeState> dsiList = null;
		DistributeState dsi = null;
		CommonResponse cr = null;

		/* 뿌리기 금액을 가져갈수있는 유저인지 확인 */
		cr = this.userValidate(roomId, userId, token);

		if (cr.getResult().equals("Y")) {

			/* 뿌리기 금액 할당 */
			dsiList = distStateRepo.findByRoomIdAndTokenAndUserId(roomId, token, -1);

			if (dsiList != null && dsiList.size() > 0) {
				dsi = dsiList.get(0);
				
				/* 할당받을 userId로 DB UPDATE */
				dsi.setUserId(userId);
				distStateRepo.save(dsi);
			
				amount = dsi.getAmount();				
				cr.setMsg(Returns.SUCCESS, Returns.SUCCESS_MSG);
				cr.setData(String.valueOf(amount));
			} else {
				cr.setResult("N");
				cr.setMsg(Returns.ERR_END, Returns.ERR_END_MSG);
			}
		}

		return cr;
	}

	/**
	 * 뿌리기 현황을 조회할수 있는 유저인지 확인
	 * 
	 * @param roomId
	 * @param masterUserId
	 * @param token
	 * @return
	 */
	public CommonResponse masterUserValidate(Distribute di, String roomId, int masterUserId, String token) {
		CommonResponse cr = new CommonResponse();
		LocalDateTime lt = LocalDateTime.now().minusDays(7); // 7일 전

		if (di == null) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_REQUEST, Returns.ERR_INVALID_REQUEST_MSG);
			return cr;
		} else {

			if (di.getMasterUserId() != masterUserId) {
				cr.setResult("N");
				cr.setMsg(Returns.ERR_ONLY_MASTER, Returns.ERR_ONLY_MASTER_MSG);
				return cr;
			}

			if (lt.isAfter(di.getCreateDate())) {
				cr.setResult("N");
				cr.setMsg(Returns.ERR_DAYOVER, Returns.ERR_DAYOVER_MSG);
				return cr;
			}
		}

		cr.setResult("Y");

		return cr;
	}

	/**
	 * 뿌리기 현황 조회
	 * 
	 * @param roomId
	 * @param masterUserId
	 * @param token
	 * @return
	 */
	public CommonResponse inqDistributedMoney(String roomId, int masterUserId, String token) {
		InqDistributeData inqData = new InqDistributeData();

		List<DistributeState> dsiList = null;
		List<Map<String, String>> distStateList = new ArrayList<>();

		Map<String, String> map = null;
		Distribute di = null;

		long distributedAmount = 0L;
		CommonResponse cr = null;

		/* 뿌리기 신청내역 조회 */
		di = distRepo.findByRoomIdAndToken(roomId, token).orElse(null);

		/* 조회권한 체크 */
		cr = this.masterUserValidate(di, roomId, masterUserId, token);

		if (cr.getResult().equals("N")) {
			return cr;
		}

		dsiList = distStateRepo.findByRoomIdAndTokenAndUserIdNot(roomId, token, -1);
		if (dsiList == null) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_FAILED, Returns.ERR_FAILED_MSG);
			return cr;
		} else {
			
			distributedAmount = 0L;
			
			if(dsiList.size() != 0) {
				for (DistributeState dsi : dsiList) {
					distributedAmount += dsi.getAmount();
					map = new HashMap<>();
					map.put("userId", String.valueOf(dsi.getUserId()));
					map.put("amount", String.valueOf(dsi.getAmount()));
					distStateList.add(map);
				}
			}
		}

		/* 응답데이터 셋팅 */
		inqData.setAmount(di.getAmount());
		inqData.setCreateDate(di.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		inqData.setDistributedAmount(distributedAmount);
		inqData.setDistStateList(distStateList);

		cr.setResult("Y");
		cr.setMsg(Returns.SUCCESS, Returns.SUCCESS_MSG);
		cr.setData(inqData);

		return cr;
	}
}
