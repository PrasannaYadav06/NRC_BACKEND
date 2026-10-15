package com.hfw.nrc.util;

/**
 * Authenticated-user context extracted from the JWT by {@link JwtRequestFilter}.
 * Mirrors com.hfw.kabhi.util.KabhiUserPrincipal's role, but NRC's role is new
 * (nr.nrc_users had no role column until this migration - see
 * db/migration/V1__add_role_to_nrc_users.sql) rather than an established
 * per-record roleId like KABHI's.
 */
public class NrcUserPrincipal {

    private final long userId;
    private final String username;
    private final String role;
    private final Integer hospitalId;

    public NrcUserPrincipal(long userId, String username, String role, Integer hospitalId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.hospitalId = hospitalId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Integer getHospitalId() {
        return hospitalId;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isRcho() {
        return "RCHO".equalsIgnoreCase(role);
    }

    public boolean isHospitalUser() {
        return "HOSPITAL_USER".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return username;
    }
}
