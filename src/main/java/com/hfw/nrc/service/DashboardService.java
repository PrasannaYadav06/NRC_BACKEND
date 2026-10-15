package com.hfw.nrc.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.repository.FollowupRepository;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcAdmissionRepository;
import com.hfw.nrc.repository.NrcHrRepository;

/**
 * Backs the NRC Data Dashboard's 5 cards. New module - no equivalent existed
 * in either source app. Shape modeled on KABHI_BACKEND's
 * KabhiHrService#getDashboardSummary (the one place KABHI itself genuinely
 * branches response *shape* by role): admin gets a District/Taluka
 * breakdown per card, a district/taluka-level user gets their own single
 * number - see the "Hospitals" card is admin-only entirely (a single
 * hospital login has nothing meaningful to break down).
 *
 * "hospitalId == null" is what the admin scoping resolves to when no
 * specific hospital was requested (SecurityUtils.resolveHospitalId) - every
 * query here treats a null hospitalId as "every hospital", matching the
 * same convention used throughout NRC_BACKEND's other admin-vs-user
 * scoping.
 */
@Service
public class DashboardService {

	@Autowired private FollowupRepository followupRepository;
	@Autowired private HospitalRepository hospitalRepository;
	@Autowired private NrcHrRepository hrRepository;
	@Autowired private NrcAdmissionRepository nrcAdmissionRepository;

	public Map<String, Object> getDashboardSummary(boolean isAdmin, Integer hospitalId, Integer month, Integer year) {
		Map<String, Object> result = new LinkedHashMap<>();

		// Stage 3: inpatient/discharge counts now read nr.nrc_admission, not
		// nr.case_details - case_details stopped receiving new admissions
		// once the consolidated module shipped (nrc_admission was backfilled
		// with its full history, so nothing is lost by switching over).
		// Stage 6: HR card now reads nr.nrc_hr, not nr.hospital_details -
		// same cutover, plus this now includes MSW (see NrcHrRepository's
		// sumHrRecruitedAndSanctioned javadoc).
		if (isAdmin) {
			result.put("hospitals", levelCounts(hospitalRepository.countHospitalsByLevel()));
			result.put("inpatient", levelCounts(nrcAdmissionRepository.countInpatientsByLevel(null, month, year)));
			result.put("discharge", levelCounts(nrcAdmissionRepository.countDischargesByLevel(null, month, year)));
			result.put("followup", levelCounts(followupRepository.countFollowupPatientsByLevel(null, month, year)));
			result.put("hr", hrCounts(hrRepository.sumHrRecruitedAndSanctioned(null, month, year)));
		} else {
			result.put("inpatient", singleCount(nrcAdmissionRepository.countInpatientsByLevel(hospitalId, month, year)));
			result.put("discharge", singleCount(nrcAdmissionRepository.countDischargesByLevel(hospitalId, month, year)));
			result.put("followup", singleCount(followupRepository.countFollowupPatientsByLevel(hospitalId, month, year)));
			result.put("hr", hrCounts(hrRepository.sumHrRecruitedAndSanctioned(hospitalId, month, year)));
		}

		return result;
	}

	/** {district: N, taluka: N} from a level-grouped [level, count] row set - missing levels default to 0. */
	private Map<String, Object> levelCounts(List<Object[]> rows) {
		Map<String, Object> out = new HashMap<>();
		out.put("district", 0L);
		out.put("taluka", 0L);
		for (Object[] row : rows) {
			String level = (String) row[0];
			long count = ((Number) row[1]).longValue();
			if ("DISTRICT".equalsIgnoreCase(level)) {
				out.put("district", count);
			} else if ("TALUKA".equalsIgnoreCase(level)) {
				out.put("taluka", count);
			}
		}
		return out;
	}

	/** A non-admin's single hospital collapses to one number regardless of its level. */
	private long singleCount(List<Object[]> rows) {
		long total = 0;
		for (Object[] row : rows) {
			total += ((Number) row[1]).longValue();
		}
		return total;
	}

	private Map<String, Object> hrCounts(List<Object[]> rows) {
		Map<String, Object> out = new HashMap<>();
		if (rows.isEmpty() || rows.get(0)[0] == null) {
			out.put("recruited", 0L);
			out.put("sanctioned", 0L);
		} else {
			Object[] row = rows.get(0);
			out.put("recruited", row[0] != null ? ((Number) row[0]).longValue() : 0L);
			out.put("sanctioned", row[1] != null ? ((Number) row[1]).longValue() : 0L);
		}
		return out;
	}
}
