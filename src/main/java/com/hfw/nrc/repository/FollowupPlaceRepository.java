package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.FollowupPlace;

public interface FollowupPlaceRepository extends JpaRepository<FollowupPlace, Integer> {

	@Query(value = "select * from config.followup_place order by place_id asc", nativeQuery = true)
	List<FollowupPlace> findAllOrdered();
}
