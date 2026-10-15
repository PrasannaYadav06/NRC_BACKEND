package com.hfw.nrc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.ChildDemography;

public interface ChildDemographyRepository extends JpaRepository<ChildDemography, Integer> {

	ChildDemography findFirstByNameAndGender(String name, String gender);

	ChildDemography findFirstByRchId(Long rchId);
}
