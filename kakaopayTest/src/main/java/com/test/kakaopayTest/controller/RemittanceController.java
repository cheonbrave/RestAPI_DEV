package com.test.kakaopayTest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.kakaopayTest.consts.Returns;
import com.test.kakaopayTest.domain.CommonResponse;
import com.test.kakaopayTest.domain.SetDistributeData;
import com.test.kakaopayTest.service.DistributeService;

@RestController
@RequestMapping(value = "/remittance")
public class RemittanceController {

	static final String ROOM_ID = "X-ROOM-ID";
	static final String USER_ID = "X-USER-ID";

	@Autowired
	DistributeService distService;

	/* 뿌리기 요청 */
	@PostMapping(value = "/distribute")
	public ResponseEntity<CommonResponse> setDistribute(
			@RequestHeader(ROOM_ID) String roomId,
			@RequestHeader(USER_ID) int masterUserId, 
			@RequestBody SetDistributeData rcvData) {

		CommonResponse cr = new CommonResponse();
		
		/* 수신값 검증 */
		if("".equals(roomId) || masterUserId < 0) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_PARAM, Returns.ERR_INVALID_PARAM_MSG);
		}
		
		if(rcvData.getAmount() <= 0 || rcvData.getCount() <= 0) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_PARAM, Returns.ERR_INVALID_PARAM_MSG);
		}
		
		if(rcvData.getAmount() < rcvData.getCount()) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_PARAM, Returns.ERR_INVALID_PARAM_MSG);
		}
		
		if("N".equals(cr.getResult())) {
			return new ResponseEntity<CommonResponse>(cr, HttpStatus.OK);
		}
		
		/* 토큰 발행 및 뿌릴 금액을 인원수에 맞게 분배하여 저장 */
		String token = distService.setDistribute(roomId, masterUserId, rcvData);
		
		/* 응답값 셋팅 */
		if (!"".equals(token)) {
			cr.setResult("Y");
			cr.setMsg(Returns.SUCCESS, Returns.SUCCESS_MSG);
			cr.setData(token);
		} else {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_FAILED, Returns.ERR_FAILED_MSG);
		}
		
		return new ResponseEntity<CommonResponse>(cr, HttpStatus.OK);
	}

	/* 뿌리기 분배받기 */
	@PutMapping(value = "/distribute/{token}")
	public ResponseEntity<CommonResponse> getDistributedMoney(
			@RequestHeader(ROOM_ID) String roomId,
			@RequestHeader(USER_ID) int userId, 
			@PathVariable(value = "token") String token) {

		CommonResponse cr = new CommonResponse();
		
		/* 수신값 검증 */
		if("".equals(roomId) || userId < 0 || token.length() != 3) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_PARAM, Returns.ERR_INVALID_PARAM_MSG);
			return new ResponseEntity<CommonResponse>(cr, HttpStatus.OK);
		}
		
		cr = distService.getDistributedMoney(roomId, userId, token);

		return new ResponseEntity<CommonResponse>(cr, HttpStatus.OK);
	}

	/* 뿌리기 내역 조회 */
	@GetMapping(value = "/distribute/{token}")
	public ResponseEntity<CommonResponse> inqDistributedMoney(
			@RequestHeader(ROOM_ID) String roomId,
			@RequestHeader(USER_ID) int masterUserId, 
			@PathVariable(value = "token") String token) {

		CommonResponse cr = new CommonResponse();
		
		/* 수신값 검증 */
		if("".equals(roomId) || masterUserId < 0 || token.length() != 3) {
			cr.setResult("N");
			cr.setMsg(Returns.ERR_INVALID_PARAM, Returns.ERR_INVALID_PARAM_MSG);
			return new ResponseEntity<CommonResponse>(cr, HttpStatus.OK);
		}

		cr = distService.inqDistributedMoney(roomId, masterUserId, token);

		return new ResponseEntity<CommonResponse>(cr, HttpStatus.OK);
	}

}
