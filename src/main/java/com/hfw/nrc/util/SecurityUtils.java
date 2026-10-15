package com.hfw.nrc.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Central place for controllers to enforce hospital-level data isolation -
 * NRC's analogue of com.hfw.kabhi.util.SecurityUtils#resolveDistrictId.
 *
 * This is a deliberate behavior change, not a silent port: today, neither
 * nrcadmin nor nrcapp verify that a client-supplied {loginUserId}/hospitalId
 * path variable actually belongs to the caller - any authenticated session
 * can pass any hospital's id. resolveHospitalId() closes that gap.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static NrcUserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof NrcUserPrincipal) {
            return (NrcUserPrincipal) principal;
        }
        return null;
    }

    /**
     * Resolves the hospitalId a query should actually run with. ADMIN gets
     * back whatever was requested (null meaning "all hospitals"); every other
     * role's requested hospitalId is ignored and replaced with their own,
     * taken from the authenticated JWT - never from client-supplied data.
     */
    public static Integer resolveHospitalId(Integer requestedHospitalId) {
        NrcUserPrincipal user = currentUser();
        if (user == null) {
            return -1; // fail closed - shouldn't happen behind the JWT filter
        }
        if (user.isAdmin()) {
            return requestedHospitalId;
        }
        return user.getHospitalId();
    }

    public static boolean isDenied(Integer resolvedHospitalId) {
        return resolvedHospitalId != null && resolvedHospitalId == -1;
    }

    /**
     * Same idea as {@link #resolveHospitalId}, for the handful of legacy
     * nrcapp endpoints keyed by {loginUserId} rather than hospitalId (e.g.
     * /getallunits/{loginUserId}). Non-admins always get their own u_id back
     * regardless of what path variable was supplied; ADMIN can query on
     * behalf of any user.
     */
    public static Integer resolveUserId(Integer requestedUserId) {
        NrcUserPrincipal user = currentUser();
        if (user == null) {
            return -1;
        }
        if (user.isAdmin()) {
            return requestedUserId;
        }
        return (int) user.getUserId();
    }
}
