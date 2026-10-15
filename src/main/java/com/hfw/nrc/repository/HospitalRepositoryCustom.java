package com.hfw.nrc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hfw.nrc.domain.Hospital;

/**
 * Multi-column ordering across the FULL result set - see
 * NrcAdmissionRepositoryCustom's javadoc for why this can't be a plain
 * {@code @Query}. Scoped to columns that live directly on nr.hospital
 * (hCode/name/nameShort) - districtName/hospitalLevel/numberOfBeds/username
 * are resolved via separate per-row lookups in HospitalAdminService#toDto
 * (district join, nr.hospital_details latest-value, nr.nrc_users first-row),
 * not part of this base query, so true cross-page sort on those would need
 * real SQL joins; left as page-only client-side sort for now.
 */
public interface HospitalRepositoryCustom {

	/** districtId/talukaId: same admin-only District/Taluka filter as In-Patient Care/Follow-up - both nullable, no-op when absent. h.district_id/h.taluka_id live directly on nr.hospital, no join needed. */
	Page<Hospital> searchSorted(String search, Integer districtId, Integer talukaId, String sort, Pageable pageable);
}
