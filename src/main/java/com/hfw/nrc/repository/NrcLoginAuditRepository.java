package com.hfw.nrc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.NrcLoginAudit;

public interface NrcLoginAuditRepository extends JpaRepository<NrcLoginAudit, Integer> {
}
