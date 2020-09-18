package com.test.kakaopayTest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.test.kakaopayTest.domain.DistributeState;

public interface DistributeStateRepository extends JpaRepository<DistributeState, Long> {

	List<DistributeState> findByRoomIdAndTokenAndUserId(String roomId, String token, int userId);

	List<DistributeState> findByRoomIdAndTokenAndUserIdNot(String roomId, String token, int userId);

}
