package com.hfw.nrc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import com.hfw.nrc.util.JwtRequestFilter;

/**
 * Modeled directly on KABHI_BACKEND's SecurityConfig. Replaces both
 * nrcadmin's HttpSession/interceptor auth and nrcapp's legacy
 * session-cookie auth with a single stateless JWT filter chain.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.cors().and()
			.csrf().disable()

			.headers()
				.cacheControl().disable()
				.addHeaderWriter(new StaticHeadersWriter(
						"Cache-Control", "no-store, no-cache, must-revalidate, max-age=0"))
				.addHeaderWriter(new StaticHeadersWriter("Pragma", "no-cache"))
				.addHeaderWriter(new StaticHeadersWriter("Expires", "0"))
				.contentSecurityPolicy(
					"default-src 'self'; " +
					"script-src 'self'; " +
					"style-src 'self'; " +
					"img-src 'self' data:; " +
					"font-src 'self'; " +
					"object-src 'none'; " +
					"base-uri 'self'; " +
					"frame-ancestors 'none'"
				)
				.and()
				.contentTypeOptions()
				.and()
				.frameOptions().sameOrigin()

			.and()
			.authorizeRequests()
				.antMatchers("/login", "/getCaptcha").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.anyRequest().authenticated()

			.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

			.and()
			// Spring Security auto-registers its own POST /logout (session-based:
			// clears the security context, 302-redirects) which was silently
			// intercepting AuthController's real /logout before it ever ran -
			// there's no session to invalidate with stateless JWTs anyway.
			.logout().disable();

		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
