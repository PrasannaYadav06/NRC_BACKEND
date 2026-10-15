package com.hfw.nrc.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.Rcho;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.RchoRepository;

/**
 * Server-side enforcement of an RCHO's district scope for the In-Patient
 * Care / Follow-up list endpoints - SecurityUtils itself is a plain static
 * utility with no repository access, so this is a small Spring-managed
 * sibling for the one case that needs a DB lookup (an RCHO's districtId
 * isn't in the JWT claims, only their own u_id is).
 *
 * Same "never trust the client-requested value for a non-admin caller"
 * principle as SecurityUtils#resolveHospitalId: an RCHO's requested
 * districtId param is always overridden with their own district, never
 * honored as-is - otherwise an RCHO could clear the frontend's pre-filled
 * filter and see another district's patient data, since the underlying
 * repository query has no district restriction of its own for a null
 * hId + null districtId combination (that combination is exactly what an
 * ADMIN uses to mean "show everything").
 */
@Component
public class RchoScopeResolver {

    @Autowired
    private RchoRepository rchoRepository;
    @Autowired
    private HospitalRepository hospitalRepository;

    /**
     * Returns the requested districtId unchanged for ADMIN; for an RCHO,
     * always their own district; for a HOSPITAL_USER (district/taluka/CHC/
     * PHC login - added for NRC Analytics access), always their own
     * hospital's district, same fail-closed-on--1 pattern as RCHO. A
     * HOSPITAL_USER only ever sees their own district's aggregate here,
     * never the per-taluka breakdown within it - see
     * NrcAnalyticsController#talukasForDistrict, which stays ADMIN/RCHO-only.
     */
    public Integer resolveDistrictId(Integer requestedDistrictId, NrcUserPrincipal principal) {
        if (principal == null) {
            return requestedDistrictId;
        }
        if (principal.isRcho()) {
            return rchoRepository.findByUId((int) principal.getUserId()).map(Rcho::getDistrictId).orElse(-1);
        }
        if (principal.isHospitalUser()) {
            Integer hospitalId = principal.getHospitalId();
            if (hospitalId == null) {
                return -1;
            }
            return hospitalRepository.findById(hospitalId).map(Hospital::getDistrictId).orElse(-1);
        }
        return requestedDistrictId;
    }

    /**
     * In-Patient Care/Follow-up's "specific PHC/CHC hospital" filter option
     * (see GeneralConfigService#getAllTaluks's includeHospitals entries) -
     * only ADMIN/RCHO may use it (a HOSPITAL_USER is already locked to their
     * own single hospital via SecurityUtils.resolveHospitalId, this filter
     * doesn't apply to them), and only for a hospital actually inside the
     * already-resolved district context - never trust the client value
     * outright. Returns null (no-op, filter ignored) if not applicable or
     * the hospital doesn't match.
     */
    public Integer resolveFilterHospitalId(Integer filterHospitalId, Integer effectiveDistrictId, NrcUserPrincipal principal) {
        if (filterHospitalId == null || principal == null || !(principal.isAdmin() || principal.isRcho())) {
            return null;
        }
        return hospitalRepository.findById(filterHospitalId)
                .filter(h -> effectiveDistrictId != null && effectiveDistrictId.equals(h.getDistrictId()))
                .map(Hospital::getHId)
                .orElse(null);
    }
}
