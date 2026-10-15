package com.hfw.nrc.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hfw.nrc.domain.NrcAdmission;

/**
 * Multi-column ordering across the FULL result set (every page), not just
 * whatever page happens to be loaded client-side. JDBC bind parameters can
 * only substitute VALUES, never column names/ASC-DESC keywords, so this
 * can't be a plain {@code @Query} like {@link NrcAdmissionRepository#search}
 * - it needs the ORDER BY clause built as real SQL text. See
 * NrcAdmissionRepositoryImpl for how that's kept injection-safe (a
 * whitelist map from the frontend's sort keys to real column names - only
 * whitelisted entries are ever concatenated into SQL, everything else is
 * silently dropped, never trusted as raw text).
 */
public interface NrcAdmissionRepositoryCustom {

	/**
	 * requireCaptures: null = no filter; 1 = only rows with at least one
	 * nr.nrc_weight_capture row; 0 = only rows with none. Needed as its own
	 * EXISTS-based filter rather than a form_status range - a large chunk of
	 * this table's real data predates the form_status state-machine (bulk-
	 * migrated/imported admissions whose weight captures were never recorded
	 * through NrcAdmissionService#recordWeightCapture, so form_status was
	 * never bumped into the 3.x range even though real capture rows exist) -
	 * see NrcAdmissionService#toListDto's statusLabel/In-Patient Care's
	 * Admitted/Under-Weight-Capture bucket for why this must be the actual
	 * source of truth instead.
	 */
	Page<NrcAdmission> searchSorted(Integer hId, String search, Integer month, Integer year,
			BigDecimal minStatus, BigDecimal maxStatus, Integer districtId, Integer talukaId,
			Integer requireCaptures, String sort, Pageable pageable);
}
