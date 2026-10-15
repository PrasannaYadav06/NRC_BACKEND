package com.hfw.nrc.util;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

/**
 * Modeled on com.hfw.kabhi.util.JwtRequestFilter, minus KABHI's
 * UserSessionManager single-active-session ("kicked out") enforcement -
 * that ceremony has no equivalent in either source app and was deliberately
 * dropped as out-of-scope overhead for NRC.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenGenerator jwtTokenGenerator;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String path = request.getRequestURI();

		// Must mirror SecurityConfig's permitAll() list exactly - a stale/expired
		// token left over in the browser (e.g. from a prior login whose 30-minute
		// JWT TTL elapsed while sitting on the login page) would otherwise get
		// verified here and 401 out a request to an endpoint that's supposed to
		// be public. Concretely: JwtInterceptor (frontend) attaches
		// Authorization to every request except ones ending in "/login", so
		// GET /getCaptcha went out with a stale Bearer token, failed
		// verifyToken(), and short-circuited with 401 below - before ever
		// reaching CaptchaController - even though /getCaptcha is permitAll().
		// The captcha image then never rendered (CaptchaComponent just shows
		// its "click refresh to try again" error state on any request failure).
		if (path.equals("/login") || path.equals("/getCaptcha")) {
			chain.doFilter(request, response);
			return;
		}

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			try {
				String token = header.substring(7).trim();
				Claims claims = jwtTokenGenerator.verifyToken(token);

				if (SecurityContextHolder.getContext().getAuthentication() == null) {
					String role = claims.get("role", String.class);
					Long userId = claims.get("userId", Long.class);
					Integer hospitalId = claims.get("hospitalId", Integer.class);

					NrcUserPrincipal principal = new NrcUserPrincipal(
							userId != null ? userId : 0L, claims.getSubject(), role, hospitalId);

					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							principal, null, Collections.singletonList(new SimpleGrantedAuthority(role)));

					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"message\":\"Invalid or expired session, please log in again\"}");
				return;
			}
		}

		chain.doFilter(request, response);
	}
}
