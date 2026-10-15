package com.hfw.nrc.web;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hfw.nrc.domain.NrcInfrastructurePhoto;
import com.hfw.nrc.service.NrcInfrastructureService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Infrastructure Module scaffold - capture-details + photo-gallery per NRC.
 * Same auth convention as the rest of the hospital-facing API (no
 * @PreAuthorize, JWT-authenticated + SecurityUtils.resolveHospitalId for
 * data isolation - a hospital login only ever sees/edits its own hospital's
 * record, ADMIN can view/edit any by passing hospitalId).
 */
@RestController
public class InfrastructureController {

	@Autowired
	private NrcInfrastructureService service;

	@GetMapping("/infrastructure")
	public ResponseEntity<Map<String, Object>> get(@RequestParam(required = false) Integer hospitalId) {
		Integer resolved = SecurityUtils.resolveHospitalId(hospitalId);
		if (SecurityUtils.isDenied(resolved)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(service.getInfrastructure(resolved));
	}

	@PostMapping("/infrastructure/details")
	public ResponseEntity<Void> saveDetails(
			@RequestParam(required = false) Integer hospitalId,
			@RequestParam String details) {
		Integer resolved = SecurityUtils.resolveHospitalId(hospitalId);
		if (SecurityUtils.isDenied(resolved)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		NrcUserPrincipal user = SecurityUtils.currentUser();
		service.saveDetails(resolved, details, user != null ? (int) user.getUserId() : null);
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/infrastructure/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> uploadPhoto(
			@RequestParam(required = false) Integer hospitalId,
			@RequestPart("file") MultipartFile file,
			@RequestParam(required = false) String caption) {
		Integer resolved = SecurityUtils.resolveHospitalId(hospitalId);
		if (SecurityUtils.isDenied(resolved)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		NrcUserPrincipal user = SecurityUtils.currentUser();
		try {
			service.uploadPhoto(resolved, file, caption, user != null ? (int) user.getUserId() : null);
			return ResponseEntity.ok().build();
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/infrastructure/photo/{photoId}")
	public ResponseEntity<byte[]> getPhoto(@PathVariable int photoId) {
		NrcInfrastructurePhoto photo = service.findPhoto(photoId);
		if (photo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		Integer resolved = SecurityUtils.resolveHospitalId(photo.getHId());
		if (SecurityUtils.isDenied(resolved) || (resolved != null && resolved != photo.getHId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		try {
			byte[] bytes = service.readPhotoBytes(photo);
			String contentType = service.probeContentType(photo);
			return ResponseEntity.ok().header("Content-Type", contentType).body(bytes);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@DeleteMapping("/infrastructure/photo/{photoId}")
	public ResponseEntity<Void> deletePhoto(@PathVariable int photoId) {
		NrcInfrastructurePhoto photo = service.findPhoto(photoId);
		if (photo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		Integer resolved = SecurityUtils.resolveHospitalId(photo.getHId());
		if (SecurityUtils.isDenied(resolved) || (resolved != null && resolved != photo.getHId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		try {
			service.deletePhoto(photo);
			return ResponseEntity.ok().build();
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
