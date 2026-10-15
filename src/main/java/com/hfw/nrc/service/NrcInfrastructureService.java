package com.hfw.nrc.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hfw.nrc.domain.NrcInfrastructure;
import com.hfw.nrc.domain.NrcInfrastructurePhoto;
import com.hfw.nrc.repository.NrcInfrastructurePhotoRepository;
import com.hfw.nrc.repository.NrcInfrastructureRepository;

/**
 * Infrastructure Module scaffold - see NrcInfrastructure's own javadoc for
 * why `details` is one free-text field rather than fixed columns. Photos
 * are stored on local disk under nrc.uploads.dir (no file-storage
 * convention existed anywhere in this codebase before now), one
 * subdirectory per hospital, served back through InfrastructureController
 * (authenticated, not a public static mapping).
 */
@Service
public class NrcInfrastructureService {

	@Autowired
	private NrcInfrastructureRepository infrastructureRepository;
	@Autowired
	private NrcInfrastructurePhotoRepository photoRepository;

	@Value("${nrc.uploads.dir}")
	private String uploadsDir;

	public Map<String, Object> getInfrastructure(int hospitalId) {
		NrcInfrastructure infra = infrastructureRepository.findByHId(hospitalId).orElse(null);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("hospitalId", hospitalId);
		result.put("details", infra != null ? infra.getDetails() : null);
		result.put("lastUpdatedOn", infra != null ? infra.getLastUpdatedOn() : null);

		List<Map<String, Object>> photos = new ArrayList<>();
		for (NrcInfrastructurePhoto p : photoRepository.findByHIdOrderByUploadedOnDesc(hospitalId)) {
			Map<String, Object> photo = new LinkedHashMap<>();
			photo.put("photoId", p.getPhotoId());
			photo.put("caption", p.getCaption());
			photo.put("uploadedOn", p.getUploadedOn());
			photos.add(photo);
		}
		result.put("photos", photos);
		return result;
	}

	public void saveDetails(int hospitalId, String details, Integer userId) {
		NrcInfrastructure infra = infrastructureRepository.findByHId(hospitalId).orElseGet(() -> {
			NrcInfrastructure n = new NrcInfrastructure();
			n.setHId(hospitalId);
			return n;
		});
		infra.setDetails(details);
		infra.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		infra.setLastUpdatedBy(userId);
		infrastructureRepository.save(infra);
	}

	public void uploadPhoto(int hospitalId, MultipartFile file, String caption, Integer userId) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Empty file.");
		}
		String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo";
		String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
		String storedName = UUID.randomUUID() + ext;

		Path hospitalDir = Paths.get(uploadsDir, String.valueOf(hospitalId));
		Files.createDirectories(hospitalDir);
		Path target = hospitalDir.resolve(storedName);
		file.transferTo(target);

		NrcInfrastructurePhoto photo = new NrcInfrastructurePhoto();
		photo.setHId(hospitalId);
		photo.setFilePath(hospitalId + "/" + storedName);
		photo.setCaption(caption);
		photo.setUploadedOn(new Timestamp(System.currentTimeMillis()));
		photo.setUploadedBy(userId);
		photoRepository.save(photo);
	}

	/** Returns the photo's bytes - caller (controller) must already have verified the photo belongs to the resolved hospitalId. */
	public byte[] readPhotoBytes(NrcInfrastructurePhoto photo) throws IOException {
		Path path = Paths.get(uploadsDir, photo.getFilePath());
		try (InputStream in = Files.newInputStream(path)) {
			return in.readAllBytes();
		}
	}

	public String probeContentType(NrcInfrastructurePhoto photo) throws IOException {
		Path path = Paths.get(uploadsDir, photo.getFilePath());
		String type = Files.probeContentType(path);
		return type != null ? type : "application/octet-stream";
	}

	public NrcInfrastructurePhoto findPhoto(int photoId) {
		return photoRepository.findById(photoId).orElse(null);
	}

	public void deletePhoto(NrcInfrastructurePhoto photo) throws IOException {
		Path path = Paths.get(uploadsDir, photo.getFilePath());
		Files.deleteIfExists(path);
		photoRepository.delete(photo);
	}
}
