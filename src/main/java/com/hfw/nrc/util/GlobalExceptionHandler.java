package com.hfw.nrc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hfw.nrc.dto.ApiError;

/**
 * One consistent error envelope for the whole app - a deliberate improvement
 * over KABHI's two overlapping, largely-bypassed global handlers
 * (web.ErrorHandler + util.GlobalExceptionHandler). New project, so this is
 * introduced from day one rather than ported as-is.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(e.getMessage()));
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ApiError> handleDisabled(DisabledException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(e.getMessage()));
	}

	/** Account lockout after repeated failed logins - see AuthService#login. */
	@ExceptionHandler(LockedException.class)
	public ResponseEntity<ApiError> handleLocked(LockedException e) {
		return ResponseEntity.status(HttpStatus.LOCKED).body(new ApiError(e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception e) {
		// Was swallowing every unhandled exception app-wide with zero logging -
		// every 500 anywhere in the app showed only "Something went wrong" on
		// both the client AND the server console, with no way to diagnose what
		// actually failed short of re-throwing/instrumenting by hand each time.
		log.error("Unhandled exception", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiError("Something went wrong. Please try again later."));
	}
}
