package com.minsait.onesait.platform.examples.iotclient4springboot.repository;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.minsait.onesait.platform.examples.iotclient4springboot.model.TestTimeOntology;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class CheckTestTimeRepository {

	@Autowired
	private TestTimeRepository testTimeRepository;

	@PostConstruct
	public void testCRUD() {
		List<TestTimeOntology> l = testTimeRepository.getTestTimeByDynamicQuery("select * from TestTime");

		log.info("" + l.get(0).getTestTime().getTimestamp().get$date());

		List<TestTimeOntology> l2 = testTimeRepository.getTestTimeByDynamicQueryNative("db.TestTime.find({})");

		log.info("" + l2.get(0).getTestTime().getTimestamp().get$date());
	}

}
