package com.hfw.nrc.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.NrcUser + nrcapp.entity.NrcUser (Phase 2)
 * - both source apps authenticate against this exact table (see AuthService).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * on ~15 other entities) are intentionally dropped - Hibernate navigation
 * conveniences with no data-integrity purpose; the service layer queries
 * child tables directly instead, matching KABHI's own minimal-relationship
 * convention.
 *
 * `role` is NEW (see db/migration/V1__add_role_to_nrc_users.sql) - neither
 * source app had any server-side admin/hospital-user distinction.
 */
@Entity
@Table(name = "nrc_users", schema = "nr")
public class NrcUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "u_id")
	private int uId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	private String password;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "last_logged_in")
	private Timestamp lastLoggedIn;

	@Column(name = "role")
	private String role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "h_id")
	private Hospital hospital;

	// Login-security hardening - see db/migration/V3__add_login_security_columns.sql
	@Column(name = "failed_attempts")
	private int failedAttempts;

	@Column(name = "lock_until")
	private Timestamp lockUntil;

	@Column(name = "last_login_ip")
	private String lastLoginIp;

	@Column(name = "last_logout_on")
	private Timestamp lastLogoutOn;

	/** True from account creation until the user's first successful change-password call - see AuthService#changePassword. */
	@Column(name = "must_change_password")
	private boolean mustChangePassword;

	public int getuId() {
		return uId;
	}

	public void setuId(int uId) {
		this.uId = uId;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Timestamp getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(Timestamp lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Hospital getHospital() {
		return hospital;
	}

	public void setHospital(Hospital hospital) {
		this.hospital = hospital;
	}

	/** Convenience accessor - hospitalId claim for the JWT (see AuthService). */
	public Integer getHospitalId() {
		return hospital != null ? hospital.getHId() : null;
	}

	public int getFailedAttempts() {
		return failedAttempts;
	}

	public void setFailedAttempts(int failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public Timestamp getLockUntil() {
		return lockUntil;
	}

	public void setLockUntil(Timestamp lockUntil) {
		this.lockUntil = lockUntil;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public Timestamp getLastLogoutOn() {
		return lastLogoutOn;
	}

	public void setLastLogoutOn(Timestamp lastLogoutOn) {
		this.lastLogoutOn = lastLogoutOn;
	}

	public boolean isMustChangePassword() {
		return mustChangePassword;
	}

	public void setMustChangePassword(boolean mustChangePassword) {
		this.mustChangePassword = mustChangePassword;
	}
}
