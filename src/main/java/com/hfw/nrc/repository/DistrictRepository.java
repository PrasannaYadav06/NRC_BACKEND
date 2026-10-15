package com.hfw.nrc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.District;

public interface DistrictRepository extends JpaRepository<District, Long> {
}
