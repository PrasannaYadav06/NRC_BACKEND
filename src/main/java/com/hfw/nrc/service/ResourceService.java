package com.hfw.nrc.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.HospitalDetail;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.HrTypeModel;
import com.hfw.nrc.dto.ResourceRequestModel;
import com.hfw.nrc.dto.ResourceResponseModel;
import com.hfw.nrc.repository.HospitalDetailRepository;
import com.hfw.nrc.repository.NrcUserRepository;

/**
 * Ported from nrcapp.service.NRCOperationServiceImpl's Group G methods
 * (saveResources/getResources - human-resources staffing counts by
 * category: officers/nurses/dieticians/cooks/tenders, plus bed count).
 * getResources is ported with reportFlag always false (the only value the
 * active /getresourceslist endpoint ever passes) - the reportFlag=true,
 * month/year-scoped branch belongs to Phase 4 (reports), not built here.
 */
@Service
public class ResourceService {

	private static final Integer[] OFFICERS = { 1, 2, 3, 4 };
	private static final Integer[] NURSES = { 5, 6, 7, 8 };
	private static final Integer[] DIETICIANS = { 9, 10, 11, 12 };
	private static final Integer[] COOKS = { 13, 14, 15, 16 };
	private static final Integer[] TENDERS = { 17, 18, 19, 20 };
	private static final int BEDS_HR_TYPE_ID = 25;

	@Autowired
	private HospitalDetailRepository hospitalDetailRepository;
	@Autowired
	private NrcUserRepository nrcUserRepository;

	private static NrcUser userRef(Integer userId) {
		NrcUser user = new NrcUser();
		user.setuId(userId);
		return user;
	}

	@Transactional
	public AbstractResponse saveResources(ResourceResponseModel resReqModel) {
		AbstractResponse res = new AbstractResponse();
		List<HospitalDetail> detailEntityList = new ArrayList<>();

		LocalDate today = LocalDate.now();
		resReqModel.setMonth(today.getMonthValue());
		resReqModel.setYear(today.getYear());

		int updatedRecNums = addHospitalDetail(detailEntityList, resReqModel, resReqModel.getOfficerList(), OFFICERS);
		updatedRecNums += addHospitalDetail(detailEntityList, resReqModel, resReqModel.getNurseList(), NURSES);
		updatedRecNums += addHospitalDetail(detailEntityList, resReqModel, resReqModel.getDieticianList(), DIETICIANS);
		updatedRecNums += addHospitalDetail(detailEntityList, resReqModel, resReqModel.getCookList(), COOKS);
		updatedRecNums += addHospitalDetail(detailEntityList, resReqModel, resReqModel.getTenderList(), TENDERS);

		List<HospitalDetail> saved = new ArrayList<>();
		if (updatedRecNums == 0 && !detailEntityList.isEmpty()) {
			saved = hospitalDetailRepository.saveAll(detailEntityList);
		}

		res.setErrorCode("0");
		res.setSuccessMessage("Success : saved " + saved.size() + " record(s) and updated " + updatedRecNums + " record(s)");
		return res;
	}

	@Transactional
	public AbstractResponse getResources(ResourceRequestModel resourceRequestModel) {
		AbstractResponse response = new AbstractResponse();

		NrcUser userEntity = nrcUserRepository.findById(resourceRequestModel.getLoginUserId()).orElse(null);
		if (userEntity == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid user ID");
			return response;
		}

		List<HospitalDetail> hospitalDetails = hospitalDetailRepository.findLatestForHospital(userEntity.getHospitalId());

		ResourceResponseModel resourceModel = new ResourceResponseModel();
		HrTypeModel officerModel = new HrTypeModel();
		HrTypeModel nurseModel = new HrTypeModel();
		HrTypeModel dieticianModel = new HrTypeModel();
		HrTypeModel cookModel = new HrTypeModel();
		HrTypeModel tenderModel = new HrTypeModel();

		for (HospitalDetail detailInd : hospitalDetails) {
			for (Integer typeVal : OFFICERS) transferResourceDetails(typeVal, detailInd, officerModel);
			for (Integer typeVal : NURSES) transferResourceDetails(typeVal, detailInd, nurseModel);
			for (Integer typeVal : DIETICIANS) transferResourceDetails(typeVal, detailInd, dieticianModel);
			for (Integer typeVal : COOKS) transferResourceDetails(typeVal, detailInd, cookModel);
			for (Integer typeVal : TENDERS) transferResourceDetails(typeVal, detailInd, tenderModel);
			if (BEDS_HR_TYPE_ID == detailInd.getHrTypeId()) {
				resourceModel.setNumberOfBeds(detailInd.getValue());
			}
		}
		resourceModel.getOfficerList().add(officerModel);
		resourceModel.getNurseList().add(nurseModel);
		resourceModel.getDieticianList().add(dieticianModel);
		resourceModel.getCookList().add(cookModel);
		resourceModel.getTenderList().add(tenderModel);

		resourceModel.setMonth(resourceRequestModel.getMonth());
		resourceModel.setYear(resourceRequestModel.getYear());
		resourceModel.setLoginUserId(resourceRequestModel.getLoginUserId());

		response.setErrorCode("0");
		response.setSuccessMessage("Returned Human Resources successfully");
		response.setResponseObject(resourceModel);
		return response;
	}

	/** Ported from NRCOperationServiceImpl#addHospitalDetail. hr_type_id % 4: 1=sanctioned, 2=working, 3=vacant, 0=training. */
	private int addHospitalDetail(List<HospitalDetail> detailEntityList, ResourceResponseModel resReqModel,
			List<HrTypeModel> dataToAdd, Integer[] typeArr) {
		int updatedRecNums = 0;
		NrcUser userEntity = nrcUserRepository.findById(resReqModel.getLoginUserId()).orElse(null);
		if (userEntity == null || dataToAdd == null) {
			return 0;
		}

		for (HrTypeModel dataInd : dataToAdd) {
			for (Integer typeVal : typeArr) {
				Integer actualVal;
				int mod = typeVal % 4;
				if (mod == 1) {
					if (dataInd.getSanctioned() == null) dataInd.setSanctioned(0);
					actualVal = dataInd.getSanctioned();
				} else if (mod == 2) {
					if (dataInd.getWorking() == null) dataInd.setWorking(0);
					actualVal = dataInd.getWorking();
				} else if (mod == 3) {
					if (dataInd.getVacant() == null) dataInd.setVacant(0);
					actualVal = dataInd.getVacant();
				} else {
					if (dataInd.getTraining() == null) dataInd.setTraining(0);
					actualVal = dataInd.getTraining();
				}

				HospitalDetail existing = hospitalDetailRepository.findByMonthYearTypeAndHospital(
						resReqModel.getMonth(), resReqModel.getYear(), typeVal, userEntity.getHospitalId());
				if (existing != null) {
					existing.setValue(actualVal);
					existing.setLastUpdatedBy(userRef(resReqModel.getLoginUserId()));
					existing.setLastUpdatedOn(new Timestamp(new Date().getTime()));
					hospitalDetailRepository.save(existing);
					updatedRecNums += 1;
				} else {
					// Source bug ported verbatim (NRCOperationServiceImpl#addHospitalDetail,
					// lines ~2543-2610): insertHospitalDetail is always called with a
					// hardcoded 0, never actualVal - the call site that would pass the real
					// submitted number is commented out in the source. Net effect (present
					// in production today): the *first* save of a given month/hr_type_id
					// combo always persists 0 regardless of what was submitted; only a
					// second save (which then hits the update branch above) actually
					// persists the real value. Not fixed here per the "faithful port"
					// decision - this already is nrcapp's live behavior.
					HospitalDetail entity = new HospitalDetail();
					entity.setCreatedBy(userRef(resReqModel.getLoginUserId()));
					entity.setCreatedOn(new Timestamp(new Date().getTime()));
					entity.setHospital(userEntity.getHospital());
					entity.setMonth(resReqModel.getMonth());
					entity.setYear(resReqModel.getYear());
					entity.setHrTypeId(typeVal);
					entity.setValue(0);
					detailEntityList.add(entity);
				}
			}
		}
		return updatedRecNums;
	}

	private static void transferResourceDetails(Integer typeVal, HospitalDetail detailInd, HrTypeModel modelTemp) {
		if (!typeVal.equals(detailInd.getHrTypeId())) {
			return;
		}
		int mod = detailInd.getHrTypeId() % 4;
		if (mod == 1) modelTemp.setSanctioned(detailInd.getValue());
		else if (mod == 2) modelTemp.setWorking(detailInd.getValue());
		else if (mod == 3) modelTemp.setVacant(detailInd.getValue());
		else modelTemp.setTraining(detailInd.getValue());
	}
}
