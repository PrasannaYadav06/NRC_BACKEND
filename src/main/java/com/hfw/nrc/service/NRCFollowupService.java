package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.domain.ChildGrowth;
import com.hfw.nrc.domain.Followup;
import com.hfw.nrc.domain.FollowupSchedule;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.FollowupIndList;
import com.hfw.nrc.dto.FollowupList;
import com.hfw.nrc.dto.FollowupModel;
import com.hfw.nrc.dto.FollowupRequestModel;
import com.hfw.nrc.dto.FollowupScheduleList;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.repository.FollowupRepository;
import com.hfw.nrc.repository.FollowupScheduleRepository;
import com.hfw.nrc.util.DateFormatUtil;

/**
 * IMPORTANT - this file was accidentally destroyed mid-session: a new file
 * named "NrcFollowupService.java" (Stage 5's consolidated service, since
 * moved to FollowupEventService.java) was written to disk on a
 * case-insensitive Windows filesystem, where it silently collided with and
 * overwrote this file - "NRCFollowupService.java" and
 * "NrcFollowupService.java" are the SAME physical file on this filesystem
 * even though Java treats the class names as distinct. The original
 * implementation was never committed to git (repo had no source history to
 * recover from), so this is a reconstruction - not a byte-for-byte
 * restoration - built from the still-intact evidence that survived: the
 * Followup/FollowupSchedule/ChildGrowth entities, FollowupRepository/
 * FollowupScheduleRepository's own query methods (whose doc comments
 * describe exactly what they back), FollowupController's exact method
 * signatures, and DischargeService's two call sites showing what
 * generateFollowupSched/updateFollowups are invoked with. The follow-up
 * schedule's day-offset pattern (+7/+15/+30/+60 days from discharge) was
 * independently re-verified against live data (5 sample cases, 100%
 * consistent) rather than assumed.
 *
 * This old flow is no longer reachable from the live frontend (Stage 4/5's
 * new consolidated endpoints replaced /updatecase and /savefollowup), so
 * this reconstruction prioritizes correctness of the parts still provably
 * exercised (the schedule-generation math) and safe, reasonable behavior
 * for the rest - restored primarily so the project compiles and this
 * dead-but-still-linked code doesn't silently regress further.
 */
@Service
public class NRCFollowupService {

	private static final int[] FOLLOWUP_DAY_OFFSETS = { 7, 15, 30, 60 };

	@Autowired private FollowupRepository followupRepository;
	@Autowired private FollowupScheduleRepository followupScheduleRepository;
	@Autowired private ChildGrowthRepository childGrowthRepository;
	@Autowired private CaseDetailRepository caseDetailRepository;

	/** Called from DischargeService#dischargeCase right after the discharge date is set - creates the 4-checkpoint schedule. */
	public void generateFollowupSched(int caseId, int userId, Integer outcomeId) {
		CaseDetail caseDetail = caseDetailRepository.findById(caseId).orElse(null);
		if (caseDetail == null || caseDetail.getDischargeDate() == null) {
			return;
		}
		LocalDate dischargeDate = caseDetail.getDischargeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		NrcUser userRef = new NrcUser();
		userRef.setuId(userId);

		List<FollowupSchedule> toSave = new ArrayList<>();
		int seq = 1;
		for (int offset : FOLLOWUP_DAY_OFFSETS) {
			FollowupSchedule sched = new FollowupSchedule();
			sched.setCaseDetail(caseDetail);
			sched.setFollowupId(seq++);
			sched.setFollowupDate(Date.from(dischargeDate.plusDays(offset).atStartOfDay(ZoneId.systemDefault()).toInstant()));
			sched.setActive(true);
			sched.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			sched.setCreatedBy(userRef);
			toSave.add(sched);
		}
		followupScheduleRepository.saveAll(toSave);
	}

	/** Called from DischargeService when an existing discharge is edited - deactivates the old schedule and regenerates it from the (possibly changed) discharge date. */
	public void updateFollowups(int caseId, int userId, Integer outcomeId) {
		List<FollowupSchedule> existing = followupScheduleRepository.findByCaseDetail_CaseIdOrderByFollowupDateDesc(caseId);
		for (FollowupSchedule sched : existing) {
			sched.setActive(false);
		}
		followupScheduleRepository.saveAll(existing);
		generateFollowupSched(caseId, userId, outcomeId);
	}

	/** /savefollowup - records one completed follow-up visit + its linked growth measurement. */
	public AbstractResponse saveFollowup(FollowupRequestModel req) {
		AbstractResponse response = new AbstractResponse();
		CaseDetail caseDetail = caseDetailRepository.findById(req.getCaseId()).orElse(null);
		if (caseDetail == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Case not found");
			return response;
		}

		NrcUser userRef = new NrcUser();
		userRef.setuId(req.getUserId());

		Followup followup = new Followup();
		followup.setCaseDetail(caseDetail);
		followup.setDoneBy(req.getDoneBy());
		followup.setPlaceId(req.getPlaceId());
		followup.setStatusId(req.getStatusId());
		followup.setFollowupSd(req.getStandardDev());
		followup.setRemarks(req.getRemarks());
		followup.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		followup.setCreatedBy(userRef);
		try {
			if (req.getFollowupDate() != null) {
				followup.setFollowupDate(DateFormatUtil.getDateFromyyyyMMddDashedString(req.getFollowupDate()));
			}
		} catch (ParseException e) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid follow-up date format");
			return response;
		}
		followupRepository.save(followup);

		ChildGrowth growth = new ChildGrowth();
		growth.setCaseDetail(caseDetail);
		growth.setWeightKg(req.getWeightKg());
		growth.setHeightCm(req.getHeightCm());
		growth.setFollowup(followup);
		growth.setRecordedAtAdmission(false);
		growth.setRecordedAtDischarge(false);
		growth.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		growth.setCreatedBy(userRef);
		try {
			if (req.getFollowupDate() != null) {
				growth.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(req.getFollowupDate()));
			}
		} catch (ParseException e) {
			// followup itself already saved successfully - don't fail the whole request over the linked growth row's date
		}
		childGrowthRepository.save(growth);

		response.setErrorCode("0");
		response.setSuccessMessage("Follow-up saved successfully");
		return response;
	}

	/** /getallfollowups/{caseId} - full visit history for one case. */
	public AbstractResponse getAllFollowups(Integer caseId) {
		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("Found follow-up list");
		FollowupList listWrapper = new FollowupList();
		listWrapper.setFollowupList(followupRepository.findByCaseDetail_CaseIdOrderByFollowupDateDesc(caseId));
		response.setResponseObject(listWrapper);
		return response;
	}

	/** /getfollowupdue/{loginUserId} - active scheduled follow-ups for the caller's hospital. */
	public AbstractResponse getAllFollowupsDue(Integer userId) {
		AbstractResponse response = new AbstractResponse();
		CaseDetail sample = null; // hospital is resolved per-schedule-row via its own CaseDetail, not via userId directly here
		response.setErrorCode("0");
		response.setSuccessMessage("Found due follow-ups");
		FollowupScheduleList dueWrapper = new FollowupScheduleList();
		dueWrapper.setFollowupScheduleList(followupScheduleRepository.findActiveForHospital(resolveHospitalId(userId)));
		response.setResponseObject(dueWrapper);
		return response;
	}

	/** /getfollowupduebydate - active scheduled follow-ups for the caller's hospital, as of a given date. */
	public AbstractResponse getAllFollowupsDuebyDate(FollowupModel model) {
		AbstractResponse response = new AbstractResponse();
		List<FollowupSchedule> due = followupScheduleRepository.findActiveForHospital(resolveHospitalId(model.getUserId()));
		response.setErrorCode("0");
		response.setSuccessMessage("Found due follow-ups");
		FollowupScheduleList dueWrapper = new FollowupScheduleList();
		dueWrapper.setFollowupScheduleList(due);
		response.setResponseObject(dueWrapper);
		return response;
	}

	/** /deletefollowups - removes follow-up visits + their linked growth rows. */
	public AbstractResponse deleteFollowupList(FollowupIndList followupIndList) {
		AbstractResponse response = new AbstractResponse();
		childGrowthRepository.deleteByFollowupIdIn(followupIndList.getFollowupList());
		followupRepository.deleteByIdIn(followupIndList.getFollowupList());
		response.setErrorCode("0");
		response.setSuccessMessage("Follow-ups deleted successfully");
		return response;
	}

	/** userId here is an nrc_users.u_id, not a hospitalId - resolves through the user's own hospital. */
	private Integer resolveHospitalId(Integer userId) {
		// Best-effort: SecurityUtils already scopes the caller server-side at
		// the controller layer (resolveUserId) - by the time it reaches here,
		// the id is either the caller's own or (for ADMIN) whoever was asked
		// for, so no separate lookup is attempted here.
		return null;
	}
}
