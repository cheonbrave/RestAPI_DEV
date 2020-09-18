package com.test.kakaopayTest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.test.kakaopayTest.domain.Distribute;

public interface DistributeRepository extends JpaRepository<Distribute, Long> {
	Optional<Distribute> findByRoomIdAndToken(String roomId, String token);
}
