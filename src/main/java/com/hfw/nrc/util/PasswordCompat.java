package com.hfw.nrc.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dual-mode password check, modeled on the pattern in
 * com.hfw.kabhi.web.KabhiUsersController#validateWebUser: if the stored
 * value looks like a bcrypt hash, verify with bcrypt; otherwise fall back to
 * cstep.nrcadmin.util.EncodeUtil's legacy scheme (Base64(salt + raw + salt)).
 * On a successful legacy match, the caller should rehash() and save the
 * bcrypt value, so every nrc_users row migrates itself to real hashing the
 * next time its owner logs in - zero manual data migration, zero downtime.
 */
@Component
public class PasswordCompat {

	private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

	private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

	@Value("${nrc.legacy.salt-prefix}")
	private String saltPrefix;

	@Value("${nrc.legacy.salt-suffix}")
	private String saltSuffix;

	public boolean matches(String rawPassword, String storedPassword) {
		if (storedPassword == null || rawPassword == null) {
			return false;
		}
		if (BCRYPT_PATTERN.matcher(storedPassword).matches()) {
			return bcrypt.matches(rawPassword, storedPassword);
		}
		return legacyEncode(rawPassword).equals(storedPassword);
	}

	public boolean isLegacy(String storedPassword) {
		return storedPassword != null && !BCRYPT_PATTERN.matcher(storedPassword).matches();
	}

	public String rehash(String rawPassword) {
		return bcrypt.encode(rawPassword);
	}

	/** cstep.nrcadmin.util.EncodeUtil#encode, ported byte-for-byte. */
	private String legacyEncode(String inputString) {
		String padded = saltPrefix + inputString + saltSuffix;
		return Base64.getEncoder().withoutPadding().encodeToString(padded.getBytes(StandardCharsets.UTF_8));
	}
}
