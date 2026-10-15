package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.NrcWeightCapture;

public interface NrcWeightCaptureRepository extends JpaRepository<NrcWeightCapture, Integer> {

	List<NrcWeightCapture> findByCaseIdOrderByCaptureDateAsc(int caseId);
}
