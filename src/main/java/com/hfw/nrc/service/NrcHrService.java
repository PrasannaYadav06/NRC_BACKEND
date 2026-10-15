package com.hfw.nrc.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.NrcHr;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.HrDetailDto;
import com.hfw.nrc.dto.HrListDto;
import com.hfw.nrc.dto.HrPageResponse;
import com.hfw.nrc.dto.HrSaveRequest;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcHrRepository;

/**
 * Stage 6 - the single service behind the consolidated HR module (replaces
 * ResourceService/hospital_details' narrow EAV writes for the /admin form -
 * see db/migration/V15 for why ResourceService itself is left untouched,
 * not deleted). One row per (hospital, month, year); submitting again in
 * the same month updates that same row in place (upsert), never creates a
 * duplicate - same "don't create multiple rows, update the same row" rule
 * applied to follow-up/weight-capture's *parent* row is naturally satisfied
 * here since HR has no separate history table to begin with.
 */
@Service
public class NrcHrService {

	@Autowired private NrcHrRepository hrRepository;
	@Autowired private HospitalRepository hospitalRepository;

	/** Self-service form prefill - the caller's own hospital's most recent submission. */
	public HrDetailDto getLatestForHospital(Integer hospitalId) {
		if (hospitalId == null) return null;
		NrcHr hr = hrRepository.findLatestForHospital(hospitalId);
		if (hr == null) return null;
		return toDetailDto(hr);
	}

	public AbstractResponse saveOrUpdate(HrSaveRequest req, Integer hospitalId, int userId) {
		AbstractResponse response = new AbstractResponse();
		if (hospitalId == null) {
			response.setErrorCode("1");
			response.setErrorMessage("No hospital associated with this account");
			return response;
		}

		LocalDate today = LocalDate.now();
		int month = today.getMonthValue();
		int year = today.getYear();

		NrcHr hr = hrRepository.findByHIdAndMonthAndYear(hospitalId, month, year);
		boolean isNew = hr == null;
		if (isNew) {
			hr = new NrcHr();
			hr.setHId(hospitalId);
			hr.setMonth(month);
			hr.setYear(year);
			hr.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			hr.setCreatedBy(userId);
		}

		hr.setNumberOfBeds(req.getNumberOfBeds());
		hr.setOfficerSanctioned(req.getOfficerSanctioned());
		hr.setOfficerWorking(req.getOfficerWorking());
		hr.setOfficerVacant(req.getOfficerVacant());
		hr.setOfficerTraining(req.getOfficerTraining());
		hr.setNurseSanctioned(req.getNurseSanctioned());
		hr.setNurseWorking(req.getNurseWorking());
		hr.setNurseVacant(req.getNurseVacant());
		hr.setNurseTraining(req.getNurseTraining());
		hr.setDieticianSanctioned(req.getDieticianSanctioned());
		hr.setDieticianWorking(req.getDieticianWorking());
		hr.setDieticianVacant(req.getDieticianVacant());
		hr.setDieticianTraining(req.getDieticianTraining());
		hr.setCookSanctioned(req.getCookSanctioned());
		hr.setCookWorking(req.getCookWorking());
		hr.setCookVacant(req.getCookVacant());
		hr.setCookTraining(req.getCookTraining());
		hr.setTenderSanctioned(req.getTenderSanctioned());
		hr.setTenderWorking(req.getTenderWorking());
		hr.setTenderVacant(req.getTenderVacant());
		hr.setTenderTraining(req.getTenderTraining());
		hr.setMswSanctioned(req.getMswSanctioned());
		hr.setMswWorking(req.getMswWorking());
		hr.setMswVacant(req.getMswVacant());
		hr.setMswTraining(req.getMswTraining());

		if (!isNew) {
			hr.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
			hr.setLastUpdatedBy(userId);
		}
		hrRepository.save(hr);

		response.setErrorCode("0");
		response.setSuccessMessage("HR details saved successfully");
		return response;
	}

	/** "Search HR Records" list - admin sees every hospital, a district/taluka user is scoped to their own via hId. */
	public HrPageResponse listHr(Integer hId, String search, Integer month, Integer year, int page, int size) {
		Page<NrcHr> pageResult = hrRepository.search(hId, search, month, year, PageRequest.of(page, size));

		HrPageResponse response = new HrPageResponse();
		List<HrListDto> rows = new ArrayList<>();
		for (NrcHr hr : pageResult.getContent()) {
			rows.add(toListDto(hr));
		}
		response.setRecords(rows);
		response.setTotalElements(pageResult.getTotalElements());
		response.setTotalPages(pageResult.getTotalPages());
		response.setPage(page);
		return response;
	}

	public HrDetailDto getDetail(int hrRecId, Integer hospitalId, boolean isAdmin) {
		Optional<NrcHr> found = hrRepository.findById(hrRecId);
		if (!found.isPresent()) return null;
		NrcHr hr = found.get();
		if (!isAdmin && (hospitalId == null || hr.getHId() != hospitalId)) return null;
		return toDetailDto(hr);
	}

	private HrListDto toListDto(NrcHr hr) {
		HrListDto dto = new HrListDto();
		dto.setHrRecId(hr.getHrRecId());
		dto.setHId(hr.getHId());
		Optional<Hospital> hospital = hospitalRepository.findById(hr.getHId());
		dto.setHospitalName(hospital.map(Hospital::getName).orElse(null));
		dto.setMonth(hr.getMonth());
		dto.setYear(hr.getYear());
		dto.setNumberOfBeds(hr.getNumberOfBeds());

		int sanctioned = nz(hr.getOfficerSanctioned()) + nz(hr.getNurseSanctioned()) + nz(hr.getDieticianSanctioned())
				+ nz(hr.getCookSanctioned()) + nz(hr.getTenderSanctioned()) + nz(hr.getMswSanctioned());
		int working = nz(hr.getOfficerWorking()) + nz(hr.getNurseWorking()) + nz(hr.getDieticianWorking())
				+ nz(hr.getCookWorking()) + nz(hr.getTenderWorking()) + nz(hr.getMswWorking());
		int vacant = nz(hr.getOfficerVacant()) + nz(hr.getNurseVacant()) + nz(hr.getDieticianVacant())
				+ nz(hr.getCookVacant()) + nz(hr.getTenderVacant()) + nz(hr.getMswVacant());
		dto.setTotalSanctioned(sanctioned);
		dto.setTotalWorking(working);
		dto.setTotalVacant(vacant);
		return dto;
	}

	private HrDetailDto toDetailDto(NrcHr hr) {
		HrDetailDto dto = new HrDetailDto();
		dto.setHrRecId(hr.getHrRecId());
		dto.setHId(hr.getHId());
		Optional<Hospital> hospital = hospitalRepository.findById(hr.getHId());
		dto.setHospitalName(hospital.map(Hospital::getName).orElse(null));
		dto.setMonth(hr.getMonth());
		dto.setYear(hr.getYear());
		dto.setNumberOfBeds(hr.getNumberOfBeds());

		dto.setOfficerSanctioned(hr.getOfficerSanctioned());
		dto.setOfficerWorking(hr.getOfficerWorking());
		dto.setOfficerVacant(hr.getOfficerVacant());
		dto.setOfficerTraining(hr.getOfficerTraining());
		dto.setNurseSanctioned(hr.getNurseSanctioned());
		dto.setNurseWorking(hr.getNurseWorking());
		dto.setNurseVacant(hr.getNurseVacant());
		dto.setNurseTraining(hr.getNurseTraining());
		dto.setDieticianSanctioned(hr.getDieticianSanctioned());
		dto.setDieticianWorking(hr.getDieticianWorking());
		dto.setDieticianVacant(hr.getDieticianVacant());
		dto.setDieticianTraining(hr.getDieticianTraining());
		dto.setCookSanctioned(hr.getCookSanctioned());
		dto.setCookWorking(hr.getCookWorking());
		dto.setCookVacant(hr.getCookVacant());
		dto.setCookTraining(hr.getCookTraining());
		dto.setTenderSanctioned(hr.getTenderSanctioned());
		dto.setTenderWorking(hr.getTenderWorking());
		dto.setTenderVacant(hr.getTenderVacant());
		dto.setTenderTraining(hr.getTenderTraining());
		dto.setMswSanctioned(hr.getMswSanctioned());
		dto.setMswWorking(hr.getMswWorking());
		dto.setMswVacant(hr.getMswVacant());
		dto.setMswTraining(hr.getMswTraining());
		return dto;
	}

	private static int nz(Integer v) { return v != null ? v : 0; }
}
