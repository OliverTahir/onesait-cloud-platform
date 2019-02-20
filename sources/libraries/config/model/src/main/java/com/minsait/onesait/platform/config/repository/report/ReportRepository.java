package com.minsait.onesait.platform.config.repository.report;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.minsait.onesait.platform.config.model.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
//extends JpaSpecificationExecutor<Report>, JpaRepository<Report, Long> {
	
	static final String FIND_ALL_ACTIVE = 
			"select r from Report r where r.active = 1";
	
	static final String FIND_ALL_ACTIVE_BY_USER_ID = 
			"select r from Report r where r.active = 1 and r.user.userId = :userId";
	
	@Query(FIND_ALL_ACTIVE)
	List<Report> findAllActive();

	@Query(FIND_ALL_ACTIVE_BY_USER_ID)
	List<Report> findAllActiveByUserId(@Param("userId") String userId);

	@EntityGraph(value = "findByIdFetchFileAndParams", type = EntityGraphType.FETCH)
	Report findById(Long id);
}
