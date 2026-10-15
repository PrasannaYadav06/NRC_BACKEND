package com.hfw.nrc.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.District;
import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.domain.Rcho;
import com.hfw.nrc.dto.RchoCredentialsDto;
import com.hfw.nrc.dto.RchoPageResponse;
import com.hfw.nrc.dto.RchoRecordDto;
import com.hfw.nrc.dto.RchoSaveRequest;
import com.hfw.nrc.repository.DistrictRepository;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.repository.RchoRepository;
import com.hfw.nrc.util.PasswordCompat;

/**
 * District RCHO management - the standalone Admin > RCHOs page, and the
 * ensureRchoForDistrict() helper HospitalAdminService calls when registering
 * a hospital in a district that has no RCHO yet. One row per district (see
 * db/migration/V26); an RCHO's own login is a plain nr.nrc_users row with
 * role='RCHO'.
 */
@Service
public class RchoService {

	@Autowired private RchoRepository rchoRepository;
	@Autowired private DistrictRepository districtRepository;
	@Autowired private HospitalRepository hospitalRepository;
	@Autowired private NrcUserRepository nrcUserRepository;
	@Autowired private PasswordCompat passwordCompat;

	private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]");

	static String slug(String s) {
		return NON_ALNUM.matcher(s == null ? "" : s.toLowerCase()).replaceAll("");
	}

	/** Result of ensureRchoForDistrict - callers needing the plaintext password (only meaningful when created=true) use this, never re-fetched from storage since only the bcrypt hash is kept. */
	public static class RchoEnsureResult {
		public final Rcho rcho;
		public final boolean created;
		public final String username;
		public final String plaintextPassword;

		RchoEnsureResult(Rcho rcho, boolean created, String username, String plaintextPassword) {
			this.rcho = rcho;
			this.created = created;
			this.username = username;
			this.plaintextPassword = plaintextPassword;
		}
	}

	/**
	 * Returns the district's existing RCHO, or silently creates one with a
	 * default name ("RCHO {DistrictName}") + auto-generated username/password
	 * if none exists yet. Called from HospitalAdminService#saveHospital so a
	 * brand-new district never blocks hospital registration on a missing RCHO.
	 */
	@Transactional
	public RchoEnsureResult ensureRchoForDistrict(int districtId, String districtName) {
		Optional<Rcho> existing = rchoRepository.findByDistrictId(districtId);
		if (existing.isPresent()) {
			NrcUser user = nrcUserRepository.findById(existing.get().getUId()).orElse(null);
			return new RchoEnsureResult(existing.get(), false, user != null ? user.getUserId() : null, null);
		}
		return createRcho(districtId, districtName, "RCHO " + districtName);
	}

	/**
	 * districtName drives the login (username/password) - never rchoName.
	 * RCHO is a rotating POSITION (an officer typically holds it for a few
	 * months before transferring), so the login must stay stable across
	 * whoever currently holds it; rchoName is purely a display label, free
	 * to rename any time via Edit RCHO with zero effect on the login. Fixed
	 * 2026-09-28 after a real bug: the standalone Add RCHO form used to
	 * slug whatever name was typed (e.g. "Dr.Abhinav" -> username
	 * "rchodrabhinav"), which would have left a stale person's name baked
	 * into the login forever once they transferred out.
	 */
	private RchoEnsureResult createRcho(int districtId, String districtName, String rchoName) {
		String slug = slug(districtName);
		String username = "rcho" + slug;
		String plaintextPassword = "Rcho" + slug + "@123456";
		Timestamp now = new Timestamp(System.currentTimeMillis());

		NrcUser user = new NrcUser();
		user.setUserId(username);
		user.setPassword(passwordCompat.rehash(plaintextPassword));
		user.setCreatedOn(now);
		user.setActive(true);
		user.setRole("RCHO");
		user.setMustChangePassword(true);
		user = nrcUserRepository.save(user);

		Rcho rcho = new Rcho();
		rcho.setDistrictId(districtId);
		rcho.setUId(user.getuId());
		rcho.setRchoName(rchoName);
		rcho.setCreatedOn(now);
		rcho = rchoRepository.save(rcho);

		return new RchoEnsureResult(rcho, true, username, plaintextPassword);
	}

	/** Admin nav bar "RCHO" page - Add RCHO form submit. Fails if the district already has one (use Edit instead). */
	@Transactional
	public RchoCredentialsDto create(RchoSaveRequest req) {
		if (rchoRepository.findByDistrictId(req.getDistrictId()).isPresent()) {
			throw new IllegalStateException("This district already has an RCHO. Use Edit instead.");
		}
		District district = districtRepository.findById((long) req.getDistrictId())
				.orElseThrow(() -> new NoSuchElementException("District not found."));
		String rchoName = (req.getRchoName() == null || req.getRchoName().trim().isEmpty())
				? "RCHO " + district.getDistrictName()
				: req.getRchoName().trim();

		RchoEnsureResult result = createRcho(req.getDistrictId(), district.getDistrictName(), rchoName);
		RchoCredentialsDto dto = new RchoCredentialsDto();
		dto.setRchoId(result.rcho.getRchoId());
		dto.setRchoName(result.rcho.getRchoName());
		dto.setUsername(result.username);
		dto.setPassword(result.plaintextPassword);
		return dto;
	}

	/** Edit RCHO form submit - renames only; district/login are fixed at creation time. */
	@Transactional
	public void update(int rchoId, String newName) {
		Rcho rcho = rchoRepository.findById(rchoId).orElseThrow(() -> new NoSuchElementException("RCHO not found."));
		rcho.setRchoName(newName);
		rcho.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		rchoRepository.save(rcho);
	}

	public RchoPageResponse list(String search, int page, int size) {
		Page<Rcho> result = rchoRepository.search(search, PageRequest.of(page, size));
		RchoPageResponse response = new RchoPageResponse();
		List<RchoRecordDto> records = new ArrayList<>();
		for (Rcho r : result.getContent()) {
			records.add(toDto(r));
		}
		response.setRecords(records);
		response.setTotalElements((int) result.getTotalElements());
		response.setTotalPages(result.getTotalPages());
		response.setPage(page);
		return response;
	}

	public RchoRecordDto getById(int rchoId) {
		return rchoRepository.findById(rchoId).map(this::toDto).orElse(null);
	}

	/** NRC Hospital Registration form's read-only RCHO field, keyed off the selected District. Null (not auto-created) until a hospital is actually registered there. */
	public RchoRecordDto getByDistrict(int districtId) {
		return rchoRepository.findByDistrictId(districtId).map(this::toDto).orElse(null);
	}

	/** Self-scoped - the calling RCHO's own record, resolved from the JWT (never a client-supplied id). Used by the frontend to lock In-Patient Care/Follow-up's District filter to this RCHO's own district. */
	public RchoRecordDto getMine(com.hfw.nrc.util.NrcUserPrincipal principal) {
		if (principal == null || !principal.isRcho()) {
			return null;
		}
		return rchoRepository.findByUId((int) principal.getUserId()).map(this::toDto).orElse(null);
	}

	/** RCHO's HR page - hId/hCode/name for every hospital in their district, so each can be a "linked DNRC/TNRC" button that drills into that hospital's own HR status table (see NrcHrPostService#getHospitalTableForRcho). */
	public List<java.util.Map<String, Object>> getMyHospitals(com.hfw.nrc.util.NrcUserPrincipal principal) {
		if (principal == null || !principal.isRcho()) {
			return java.util.Collections.emptyList();
		}
		Rcho rcho = rchoRepository.findByUId((int) principal.getUserId()).orElse(null);
		if (rcho == null) {
			return java.util.Collections.emptyList();
		}
		return hospitalRepository.findAll().stream()
				.filter(h -> h.getDistrictId() != null && h.getDistrictId() == rcho.getDistrictId())
				.sorted(java.util.Comparator.comparing(Hospital::getName, java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(h -> {
					java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
					m.put("hId", h.getHId());
					m.put("hCode", h.getHCode());
					m.put("name", h.getName());
					return m;
				})
				.collect(Collectors.toList());
	}

	private RchoRecordDto toDto(Rcho r) {
		RchoRecordDto dto = new RchoRecordDto();
		dto.setRchoId(r.getRchoId());
		dto.setDistrictId(r.getDistrictId());
		dto.setRchoName(r.getRchoName());
		districtRepository.findById((long) r.getDistrictId()).ifPresent(d -> dto.setDistrictName(d.getDistrictName()));
		nrcUserRepository.findById(r.getUId()).ifPresent(u -> dto.setUsername(u.getUserId()));

		List<Hospital> hospitals = hospitalRepository.findAll().stream()
				.filter(h -> h.getDistrictId() != null && h.getDistrictId() == r.getDistrictId())
				.collect(Collectors.toList());
		dto.setHospitalCount(hospitals.size());
		dto.setHospitalNames(hospitals.stream().map(Hospital::getName).collect(Collectors.toList()));
		return dto;
	}
}
