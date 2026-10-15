package com.hfw.nrc.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.NrcLoginAudit;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.ChangePasswordRequestModel;
import com.hfw.nrc.dto.LoginResponse;
import com.hfw.nrc.repository.NrcLoginAuditRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.util.CaptchaService;
import com.hfw.nrc.util.JwtTokenGenerator;
import com.hfw.nrc.util.PasswordCompat;

/**
 * Single login path for both roles - see plan's "Recommendation on
 * login/role ceremony": nrc_users is one table (unlike KABHI's single-table
 * + roleId switch, but still just one table), so there is no admin-table
 * lookup to try first. Sources: nrcadmin's AdminService#loginUser +
 * nrcapp's NRCUserServiceImpl#loginNRCUser (both authenticate this same
 * table with the same EncodeUtil scheme).
 */
@Service
public class AuthService {

	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final long LOCK_DURATION_MS = 15 * 60_000L; // 15 minutes, matches KABHI

	@Autowired
	private NrcUserRepository nrcUserRepository;

	@Autowired
	private PasswordCompat passwordCompat;

	@Autowired
	private JwtTokenGenerator jwtTokenGenerator;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private NrcLoginAuditRepository loginAuditRepository;

	/**
	 * Captcha + account-lockout hardening added on top of the original
	 * login flow, modeled on KABHI_BACKEND's KabhiUsersController#validateWebUser
	 * (5 failed attempts -> 15 minute lock, captcha checked before the
	 * password). ipAddress may be null (e.g. a direct unit-style call) -
	 * only used for the audit trail, never for auth decisions.
	 */
	public LoginResponse login(String username, String password, String captchaId, String captchaAnswer, String ipAddress) {
		if (!captchaService.verify(captchaId, captchaAnswer)) {
			throw new BadCredentialsException("Incorrect captcha, please try again");
		}

		NrcUser user = nrcUserRepository.findByUserId(username);
		if (user == null) {
			audit(null, username, "LOGIN_FAILURE", ipAddress);
			throw new BadCredentialsException("Invalid username or password");
		}
		if (!user.isActive()) {
			throw new DisabledException("This account has been deactivated");
		}
		if (user.getLockUntil() != null && user.getLockUntil().getTime() > System.currentTimeMillis()) {
			audit(user.getuId(), username, "LOCKOUT", ipAddress);
			throw new LockedException("Account locked due to repeated failed attempts. Try again after "
					+ (user.getLockUntil().getTime() - System.currentTimeMillis()) / 60_000 + " more minute(s).");
		}

		if (!passwordCompat.matches(password, user.getPassword())) {
			user.setFailedAttempts(user.getFailedAttempts() + 1);
			if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
				user.setLockUntil(new Timestamp(System.currentTimeMillis() + LOCK_DURATION_MS));
			}
			nrcUserRepository.save(user);
			audit(user.getuId(), username, "LOGIN_FAILURE", ipAddress);
			throw new BadCredentialsException("Invalid username or password");
		}

		// Legacy accounts silently migrate to bcrypt on next successful login -
		// zero manual data migration, zero behavior change for the user.
		if (passwordCompat.isLegacy(user.getPassword())) {
			user.setPassword(passwordCompat.rehash(password));
		}
		user.setFailedAttempts(0);
		user.setLockUntil(null);
		user.setLastLoggedIn(new Timestamp(System.currentTimeMillis()));
		user.setLastLoginIp(ipAddress);
		nrcUserRepository.save(user);
		audit(user.getuId(), username, "LOGIN_SUCCESS", ipAddress);

		String role = user.getRole() != null ? user.getRole() : "HOSPITAL_USER";
		String token = jwtTokenGenerator.generateToken(user.getuId(), user.getUserId(), role, user.getHospitalId());

		return new LoginResponse(token, user.getUserId(), role, user.getHospitalId(), user.getuId(), user.isMustChangePassword());
	}

	/** No server-side session to invalidate (stateless JWT, same as KABHI) - just records the event and a timestamp. */
	public void logout(Integer userId, String username, String ipAddress) {
		if (userId != null) {
			nrcUserRepository.findById(userId).ifPresent(u -> {
				u.setLastLogoutOn(new Timestamp(System.currentTimeMillis()));
				nrcUserRepository.save(u);
			});
		}
		audit(userId, username, "LOGOUT", ipAddress);
	}

	private void audit(Integer uId, String userId, String eventType, String ipAddress) {
		NrcLoginAudit entry = new NrcLoginAudit();
		entry.setuId(uId);
		entry.setUserId(userId);
		entry.setEventType(eventType);
		entry.setIpAddress(ipAddress);
		entry.setEventOn(new Timestamp(System.currentTimeMillis()));
		loginAuditRepository.save(entry);
	}

	/**
	 * Ported from nrcapp.service.NRCUserServiceImpl#changePassword. Uses
	 * PasswordCompat (bcrypt-or-legacy) for the old-password check instead
	 * of the source's raw EncodeUtil.encode comparison, matching login()'s
	 * dual-mode handling - and always writes the new password as a real
	 * bcrypt hash, same as every other password-write path in this project.
	 */
	public AbstractResponse changePassword(ChangePasswordRequestModel cpReqModel) {
		AbstractResponse res = new AbstractResponse();

		if (cpReqModel.getUserId() == null || cpReqModel.getUserId().isEmpty()) {
			res.setErrorCode("1");
			res.setErrorMessage("User id not provided");
			return res;
		}

		NrcUser user = nrcUserRepository.findByUserId(cpReqModel.getUserId());
		if (user == null || !passwordCompat.matches(cpReqModel.getOldPassword(), user.getPassword())) {
			res.setErrorCode("1");
			res.setErrorMessage("Invalid Old password provided");
			return res;
		}

		if (cpReqModel.getNewPassword() != null && cpReqModel.getNewPassword().equals(cpReqModel.getOldPassword())) {
			res.setErrorCode("1");
			res.setErrorMessage("Both old and new passwords are equal. Provide a new password.");
			return res;
		}

		if (cpReqModel.getNewPassword() == null || cpReqModel.getNewPassword().isEmpty()) {
			res.setErrorCode("1");
			res.setErrorMessage("Provide a valid new password");
			return res;
		}

		user.setPassword(passwordCompat.rehash(cpReqModel.getNewPassword()));
		user.setMustChangePassword(false);
		nrcUserRepository.save(user);

		res.setErrorCode("0");
		res.setSuccessMessage("Password changed successfully for the user id: " + user.getUserId());
		return res;
	}
}
