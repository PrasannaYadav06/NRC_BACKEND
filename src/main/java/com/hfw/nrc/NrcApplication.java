package com.hfw.nrc;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Unified NRC backend - replaces the separately-maintained nrcadmin (Spring
 * Boot 2.7.2, session auth) and nrcapp (legacy Spring 4.3, session auth)
 * projects with one JWT-secured Spring Boot app, structured like KABHI_BACKEND.
 */
@SpringBootApplication
public class NrcApplication {

	public static void main(String[] args) {
		SpringApplication.run(NrcApplication.class, args);
	}

	/**
	 * Collapses nrcadmin's per-controller {@code @CrossOrigin} annotations and
	 * nrcapp's dual CORS filters (raw servlet Filter + WebMvcConfigurer) into a
	 * single bean, same as KabhiApplication#corsFilter. Origins are configured
	 * (not hardcoded) via {@code allowed.origins}, matching both source apps'
	 * Angular dev origins by default.
	 */
	@Bean
	public CorsFilter corsFilter(@Value("${allowed.origins:http://localhost:4200}") String allowedOrigins) {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		List<String> origins = Arrays.asList(allowedOrigins.split("\s*,\s*"));
		config.setAllowedOrigins(origins);
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowedMethods(Arrays.asList("OPTIONS", "GET", "POST", "PUT", "DELETE"));
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
