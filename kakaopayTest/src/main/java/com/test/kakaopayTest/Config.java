package com.test.kakaopayTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.kakaopayTest.repository.DistributeRepository;
import com.test.kakaopayTest.repository.DistributeStateRepository;
import com.test.kakaopayTest.service.DistributeService;

@Configuration
public class Config {

	private final DistributeRepository distRepo;
	private final DistributeStateRepository distStateRepo;

	@Autowired
	public Config(DistributeRepository distRepo, DistributeStateRepository distStateRepo) {
		this.distRepo = distRepo;
		this.distStateRepo = distStateRepo;
	}

	@Bean
	public DistributeService distributeService() {
		return new DistributeService(distRepo, distStateRepo);
	}

}
