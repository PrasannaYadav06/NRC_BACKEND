package com.hfw.nrc.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Modeled on com.hfw.kabhi.util.JwtTokenGenerator. Drops KABHI's
 * generateShortLivedToken (that's an OTP/2FA-adjacent flow with no NRC
 * equivalent).
 */
@Component
public class JwtTokenGenerator {

	@Value("${jwtSecretKey}")
	private String secretPassword;

	@Value("${jwtTimeout}")
	private long tokenTimeout;

	public String generateToken(long userId, String username, String role, Integer hospitalId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + tokenTimeout);

		return Jwts.builder()
				.setSubject(username)
				.claim("userId", userId)
				.claim("role", role)
				.claim("hospitalId", hospitalId)
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(SignatureAlgorithm.HS256, secretPassword)
				.compact();
	}

	public Claims verifyToken(String token) {
		return Jwts.parser().setSigningKey(secretPassword).parseClaimsJws(token).getBody();
	}
}
