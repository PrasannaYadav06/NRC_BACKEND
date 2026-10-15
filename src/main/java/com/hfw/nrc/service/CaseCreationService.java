package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.AdmissionComorbidCondition;
import com.hfw.nrc.domain.AdmissionCriteria;
import com.hfw.nrc.domain.AdmissionType;
import com.hfw.nrc.domain.CaseAllocation;
import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.domain.CaseMotherDetail;
import com.hfw.nrc.domain.CaseMotherGrowth;
import com.hfw.nrc.domain.CaseMotherSocialFactor;
import com.hfw.nrc.domain.CaseReferral;
import com.hfw.nrc.domain.ChildDemography;
import com.hfw.nrc.domain.ChildGrowth;
import com.hfw.nrc.domain.District;
import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.MotherDemogrphy;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.AdmissionComorbidModel;
import com.hfw.nrc.dto.AdmissionCriteriaModel;
import com.hfw.nrc.dto.CaseAllocationModel;
import com.hfw.nrc.dto.CaseDetailsRequestModel;
import com.hfw.nrc.dto.CaseMotherDetailModel;
import com.hfw.nrc.dto.CaseMotherGrowthModel;
import com.hfw.nrc.dto.CaseMotherSocialFactorModel;
import com.hfw.nrc.dto.CaseReferralModel;
import com.hfw.nrc.dto.NRCChildGrowthModel;
import com.hfw.nrc.dto.SDRequestModel;
import com.hfw.nrc.dto.ViewCaseRequestModel;
import com.hfw.nrc.repository.AdmissionComorbidConditionRepository;
import com.hfw.nrc.repository.AdmissionCriteriaRepository;
import com.hfw.nrc.repository.AdmissionCriteriaValueRepository;
import com.hfw.nrc.repository.AdmissionTypeRepository;
import com.hfw.nrc.repository.CaseAllocationRepository;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.CaseMotherDetailRepository;
import com.hfw.nrc.repository.CaseMotherGrowthRepository;
import com.hfw.nrc.repository.CaseMotherSocialFactorRepository;
import com.hfw.nrc.repository.CaseReferralRepository;
import com.hfw.nrc.repository.CasteRepository;
import com.hfw.nrc.repository.ChildDemographyRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.repository.ComorbidConditionValueRepository;
import com.hfw.nrc.repository.DistrictRepository;
import com.hfw.nrc.repository.EducationQualificationRepository;
import com.hfw.nrc.repository.MotherDemogrphyRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.repository.ReferredFromRepository;
import com.hfw.nrc.repository.ReligionRepository;
import com.hfw.nrc.repository.RiskCategoryRepository;
import com.hfw.nrc.repository.SocialFactorRepository;
import com.hfw.nrc.repository.UnitRepository;
import com.hfw.nrc.repository.WardRepository;
import com.hfw.nrc.util.DateFormatUtil;
import com.hfw.nrc.util.NRCDateException;

/**
 * Ported from nrcapp.service.NRCOperationServiceImpl's create-case flow
 * (createCase/registerMother/registerChild/updateMother/updateChild/
 * registerCase + the 8 add*Data sub-record helpers). Split into its own
 * service class (unlike the source's single 2,685-line class) purely for
 * maintainability - logic and messages are otherwise ported verbatim.
 *
 * Persistence note: the source relies on JPA cascade (CaseDetail's
 * @OneToMany sub-record collections, dropped in Phase 2 as audit-trail
 * navigation) to bulk-insert child records when the parent case is saved.
 * Since those collections don't exist here, each add*Data method instead
 * explicitly saves its own child entities via their own repository after
 * the parent CaseDetail has been persisted and has a generated caseId -
 * same net result (one atomic transaction, same rows), different mechanism.
 */
@Service
public class CaseCreationService {

	private static final int PINCODE_LENGTH = 6;

	@Autowired private CaseDetailRepository caseDetailRepository;
	@Autowired private MotherDemogrphyRepository motherDemogrphyRepository;
	@Autowired private ChildDemographyRepository childDemographyRepository;
	@Autowired private DistrictRepository districtRepository;
	@Autowired private CasteRepository casteRepository;
	@Autowired private ReligionRepository religionRepository;
	@Autowired private EducationQualificationRepository educationQualificationRepository;
	@Autowired private AdmissionTypeRepository admissionTypeRepository;
	@Autowired private AdmissionComorbidConditionRepository admissionComorbidConditionRepository;
	@Autowired private ComorbidConditionValueRepository comorbidConditionValueRepository;
	@Autowired private AdmissionCriteriaRepository admissionCriteriaRepository;
	@Autowired private AdmissionCriteriaValueRepository admissionCriteriaValueRepository;
	@Autowired private CaseAllocationRepository caseAllocationRepository;
	@Autowired private UnitRepository unitRepository;
	@Autowired private WardRepository wardRepository;
	@Autowired private CaseMotherDetailRepository caseMotherDetailRepository;
	@Autowired private RiskCategoryRepository riskCategoryRepository;
	@Autowired private CaseMotherGrowthRepository caseMotherGrowthRepository;
	@Autowired private CaseMotherSocialFactorRepository caseMotherSocialFactorRepository;
	@Autowired private SocialFactorRepository socialFactorRepository;
	@Autowired private CaseReferralRepository caseReferralRepository;
	@Autowired private ReferredFromRepository referredFromRepository;
	@Autowired private ChildGrowthRepository childGrowthRepository;
	@Autowired private NrcUserRepository nrcUserRepository;
	@Autowired private NRCOperationService nrcOperationService;

	private static NrcUser userRef(int userId) {
		NrcUser user = new NrcUser();
		user.setuId(userId);
		return user;
	}

	@Transactional
	public AbstractResponse createCase(CaseDetailsRequestModel caseDetailsModel) {
		AbstractResponse response = new AbstractResponse();

		Integer samExists = caseDetailRepository.countSamExists(caseDetailsModel.getSamNumber(),
				caseDetailsModel.getHospitalId());
		if (samExists != null && samExists == 1) {
			response.setErrorCode("2");
			response.setErrorMessage("Given sam number already exists");
			return response;
		}

		if (String.valueOf(caseDetailsModel.getPinCode()).length() != PINCODE_LENGTH) {
			response.setErrorCode("2");
			response.setErrorMessage("Enter a valid pincode");
			return response;
		}

		Integer childId;
		Integer motherId;

		// Re-admission / Relapse: an existing child (by name+gender) reuses its mother/child records.
		if (caseDetailsModel.getCaseId() != null) {
			String genVal = genderCode(caseDetailsModel.getGender());
			ChildDemography childRec = childDemographyRepository.findFirstByNameAndGender(
					caseDetailsModel.getChildName(), genVal);

			if (childRec != null && childRec.getName().equals(caseDetailsModel.getChildName())
					&& childRec.getAgeInMonths() / 12 == caseDetailsModel.getChildAgeinYears()
					&& childRec.getAgeInMonths() % 12 == caseDetailsModel.getChildAgeinMonths()) {

				motherId = childRec.getMotherDemogrphy() != null ? childRec.getMotherDemogrphy().getMotherId() : null;
				updateMother(caseDetailsModel);

				if (childRec.getRchId() != null && caseDetailsModel.getRchId() != null
						&& !childRec.getRchId().equals(caseDetailsModel.getRchId())) {
					ChildDemography dup = childDemographyRepository.findFirstByRchId(caseDetailsModel.getRchId());
					if (dup != null) {
						response.setErrorCode("2");
						response.setErrorMessage("Please enter correct RCH ID for this child");
						return response;
					}
				}

				updateChild(caseDetailsModel);
				motherId = caseDetailsModel.getMotherId();
				childId = caseDetailsModel.getChildId();
			} else {
				motherId = registerMother(caseDetailsModel);
				childId = registerChild(motherId, caseDetailsModel);
			}
		} else {
			// Fresh registration.
			if (caseDetailsModel.getRchId() != null
					&& childDemographyRepository.findFirstByRchId(caseDetailsModel.getRchId()) != null) {
				response.setErrorCode("2");
				response.setErrorMessage("Please enter correct RCH ID for this child");
				return response;
			}
			motherId = registerMother(caseDetailsModel);
			childId = registerChild(motherId, caseDetailsModel);
		}

		Integer caseId;
		try {
			caseId = registerCase(motherId, childId, caseDetailsModel);
		} catch (NRCDateException e) {
			response.setErrorCode("1");
			response.setErrorMessage(e.getExceptionMessage());
			return response;
		}

		if (caseId != null) {
			response.setErrorCode("0");
			response.setSuccessMessage("Admission success");
		} else {
			response.setErrorCode("1");
			response.setErrorMessage("Admission failed");
		}
		return response;
	}

	private static String genderCode(String gender) {
		if ("Male".equals(gender)) return "M";
		if ("Female".equals(gender)) return "F";
		if ("Others".equals(gender)) return "T";
		return "";
	}

	private Integer registerMother(CaseDetailsRequestModel caseDetailsModel) {
		MotherDemogrphy motherEntity = new MotherDemogrphy();
		transferMotherModelToMotherEntity(caseDetailsModel, motherEntity, false);

		if (caseDetailsModel.getMotherDob() != null) {
			try {
				motherEntity.setDateOfBirth(DateFormatUtil.getDateFromyyyyMMddDashedString(caseDetailsModel.getMotherDob()));
			} catch (ParseException ignored) {
				// Invalid format - matches source, which logs and continues rather than failing the request.
			}
		}

		if (caseDetailsModel.getEducationQualificationId() != null) {
			educationQualificationRepository.findById(caseDetailsModel.getEducationQualificationId())
					.ifPresent(q -> motherEntity.setEducationQualificationId(q.getEducationQualificationId()));
		}

		return motherDemogrphyRepository.save(motherEntity).getMotherId();
	}

	private Integer registerChild(Integer motherId, CaseDetailsRequestModel caseDetailsModel) {
		ChildDemography childEntity = new ChildDemography();
		transferChildModelToChildEntity(caseDetailsModel, childEntity, false);

		MotherDemogrphy mother = new MotherDemogrphy();
		mother.setMotherId(motherId);
		childEntity.setMotherDemogrphy(mother);

		if (caseDetailsModel.getChildDob() != null) {
			try {
				childEntity.setDateOfBirth(DateFormatUtil.getDateFromyyyyMMddDashedString(caseDetailsModel.getChildDob()));
			} catch (ParseException ignored) {
			}
		}

		if (caseDetailsModel.getDistrictId() != null) {
			districtRepository.findById(caseDetailsModel.getDistrictId().longValue())
					.ifPresent(d -> childEntity.setDistrictId(d.getDistrictId().intValue()));
		}
		if (caseDetailsModel.getCasteId() != null) {
			casteRepository.findById(caseDetailsModel.getCasteId()).ifPresent(c -> childEntity.setCasteId(c.getCasteId()));
		}
		if (caseDetailsModel.getReligionId() != null) {
			religionRepository.findById(caseDetailsModel.getReligionId())
					.ifPresent(r -> childEntity.setReligonId(r.getReligionId()));
		}

		return childDemographyRepository.save(childEntity).getChildId();
	}

	private AbstractResponse updateMother(CaseDetailsRequestModel caseDetailsModel) {
		AbstractResponse response = new AbstractResponse();
		CaseDetail cd = caseDetailRepository.findByCaseIdAndHospitalId(caseDetailsModel.getCaseId(), caseDetailsModel.getHospitalId());
		if (cd == null) {
			return response;
		}
		caseDetailsModel.setMotherId(cd.getMotherDemogrphy().getMotherId());

		motherDemogrphyRepository.findById(caseDetailsModel.getMotherId()).ifPresent(motherRecord -> {
			transferMotherModelToMotherEntity(caseDetailsModel, motherRecord, true);
			if (caseDetailsModel.getMotherDob() != null) {
				try {
					motherRecord.setDateOfBirth(DateFormatUtil.getDateFromyyyyMMddDashedString(caseDetailsModel.getMotherDob()));
				} catch (ParseException ignored) {
				}
			}
			if (caseDetailsModel.getEducationQualificationId() != null) {
				educationQualificationRepository.findById(caseDetailsModel.getEducationQualificationId())
						.ifPresent(q -> motherRecord.setEducationQualificationId(q.getEducationQualificationId()));
			}
			motherRecord.setLastUpdatedBy(userRef(caseDetailsModel.getLoginUserId()));
			motherRecord.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			motherDemogrphyRepository.save(motherRecord);
			response.setErrorCode("0");
			response.setSuccessMessage("Updated mother record successfully");
		});
		return response;
	}

	/** Public - also backs the standalone /updatechild endpoint (CaseManagementController). */
	@Transactional
	public AbstractResponse updateChild(CaseDetailsRequestModel caseDetailsModel) {
		AbstractResponse response = new AbstractResponse();
		CaseDetail cd = caseDetailRepository.findByCaseIdAndHospitalId(caseDetailsModel.getCaseId(), caseDetailsModel.getHospitalId());
		if (cd == null) {
			return response;
		}
		caseDetailsModel.setChildId(cd.getChildDemography().getChildId());

		childDemographyRepository.findById(caseDetailsModel.getChildId()).ifPresent(childRecord -> {
			transferChildModelToChildEntity(caseDetailsModel, childRecord, true);
			if (caseDetailsModel.getChildDob() != null) {
				try {
					childRecord.setDateOfBirth(DateFormatUtil.getDateFromyyyyMMddDashedString(caseDetailsModel.getChildDob()));
				} catch (ParseException ignored) {
				}
			}
			if (caseDetailsModel.getDistrictId() != null) {
				districtRepository.findById(caseDetailsModel.getDistrictId().longValue())
						.ifPresent(d -> childRecord.setDistrictId(d.getDistrictId().intValue()));
			}
			if (caseDetailsModel.getCasteId() != null) {
				casteRepository.findById(caseDetailsModel.getCasteId()).ifPresent(c -> childRecord.setCasteId(c.getCasteId()));
			}
			if (caseDetailsModel.getReligionId() != null) {
				religionRepository.findById(caseDetailsModel.getReligionId())
						.ifPresent(r -> childRecord.setReligonId(r.getReligionId()));
			}
			childDemographyRepository.save(childRecord);
			response.setErrorCode("0");
			response.setSuccessMessage("Updated child record successfully");
		});
		return response;
	}

	private Integer registerCase(Integer motherId, Integer childId, CaseDetailsRequestModel caseDetailsModel) throws NRCDateException {
		CaseDetail caseDetailEntity = new CaseDetail();
		transferCaseRequestModelToCaseDetailEntity(caseDetailsModel, caseDetailEntity, false);

		ChildDemography child = new ChildDemography();
		child.setChildId(childId);
		caseDetailEntity.setChildDemography(child);

		MotherDemogrphy mother = new MotherDemogrphy();
		mother.setMotherId(motherId);
		caseDetailEntity.setMotherDemogrphy(mother);

		try {
			caseDetailEntity.setAdmissionDate(DateFormatUtil.getTimestampFromDashedDateString(caseDetailsModel.getAdmissionDate()));
		} catch (ParseException e) {
			throw new NRCDateException("Invalid admission date");
		}

		AdmissionType typeObj = admissionTypeRepository.findById(caseDetailsModel.getAdmissionTypeId()).orElse(null);
		if (typeObj != null) {
			caseDetailEntity.setAdmissionTypeId(typeObj.getAdmissionTypeId());
		}
		caseDetailEntity.setSamNum(caseDetailsModel.getSamNumber());

		// Save the parent first - child sub-records below need a real caseId
		// (no JPA cascade here, see class javadoc).
		caseDetailEntity = caseDetailRepository.save(caseDetailEntity);

		addComorbidData(caseDetailEntity, caseDetailsModel, false);
		addAdmissionCriteriaData(caseDetailEntity, caseDetailsModel, false);
		addCaseAllocData(caseDetailEntity, caseDetailsModel, false);
		addMotherDetails(caseDetailEntity, caseDetailsModel, false);
		addMotherGrowth(caseDetailEntity, caseDetailsModel, false);
		addMotherSocialFactors(caseDetailEntity, caseDetailsModel, false);
		addCaseReferrals(caseDetailEntity, caseDetailsModel, false);
		addChildGrowth(caseDetailEntity, caseDetailsModel, false); // sets admissionSd on caseDetailEntity

		caseDetailRepository.save(caseDetailEntity); // persist the admissionSd computed above

		return caseDetailEntity.getCaseId();
	}

	/** Ported from NRCOperationServiceImpl#updateAdmission - the /updateadmission endpoint. */
	@Transactional
	public AbstractResponse updateAdmission(CaseDetailsRequestModel caseDetailsModel) {
		AbstractResponse response = new AbstractResponse();

		if (String.valueOf(caseDetailsModel.getPinCode()).length() != PINCODE_LENGTH) {
			response.setErrorCode("2");
			response.setErrorMessage("Enter a valid pincode");
			return response;
		}

		if (caseDetailsModel.getCaseId() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Please enter a valid case id");
			return response;
		}

		CaseDetail caseDetailEntity = caseDetailRepository.findByCaseIdAndHospitalId(caseDetailsModel.getCaseId(), caseDetailsModel.getHospitalId());
		if (caseDetailEntity == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Please enter a valid case id");
			return response;
		}

		if (caseDetailEntity.getSamNum() != caseDetailsModel.getSamNumber()) {
			Integer samExists = caseDetailRepository.countSamExists(caseDetailsModel.getSamNumber(), caseDetailsModel.getHospitalId());
			if (samExists != null && samExists == 1) {
				response.setErrorCode("2");
				response.setErrorMessage("Given sam number already exists");
				return response;
			}
		}

		caseDetailsModel.setMotherId(caseDetailEntity.getMotherDemogrphy().getMotherId());
		MotherDemogrphy motherRecord = motherDemogrphyRepository.findById(caseDetailsModel.getMotherId()).orElse(null);

		caseDetailsModel.setChildId(caseDetailEntity.getChildDemography().getChildId());
		ChildDemography childRecord = childDemographyRepository.findById(caseDetailsModel.getChildId()).orElse(null);

		transferCaseRequestModelToCaseDetailEntity(caseDetailsModel, caseDetailEntity, true);
		updateChild(caseDetailsModel);
		updateMother(caseDetailsModel);

		caseDetailEntity.setMotherDemogrphy(motherRecord);
		caseDetailEntity.setChildDemography(childRecord);

		try {
			caseDetailEntity.setAdmissionDate(DateFormatUtil.getTimestampFromDashedDateString(caseDetailsModel.getAdmissionDate()));
		} catch (ParseException e) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid date format in admission date");
			return response;
		}

		AdmissionType typeObj = admissionTypeRepository.findById(caseDetailsModel.getAdmissionTypeId()).orElse(null);
		if (typeObj != null) {
			caseDetailEntity.setAdmissionTypeId(typeObj.getAdmissionTypeId());
		}
		caseDetailEntity.setSamNum(caseDetailsModel.getSamNumber());

		addComorbidData(caseDetailEntity, caseDetailsModel, true);
		addAdmissionCriteriaData(caseDetailEntity, caseDetailsModel, true);
		addCaseAllocData(caseDetailEntity, caseDetailsModel, true);
		try {
			addMotherDetails(caseDetailEntity, caseDetailsModel, true);
		} catch (NRCDateException e) {
			response.setErrorCode("1");
			response.setErrorMessage(e.getExceptionMessage());
			return response;
		}
		try {
			addMotherGrowth(caseDetailEntity, caseDetailsModel, true);
		} catch (NRCDateException e) {
			response.setErrorCode("1");
			response.setErrorMessage(e.getExceptionMessage());
			return response;
		}
		addMotherSocialFactors(caseDetailEntity, caseDetailsModel, true);
		addCaseReferrals(caseDetailEntity, caseDetailsModel, true);
		try {
			addChildGrowth(caseDetailEntity, caseDetailsModel, true);
		} catch (NRCDateException e) {
			response.setErrorCode("1");
			response.setErrorMessage(e.getExceptionMessage());
			return response;
		}

		caseDetailRepository.save(caseDetailEntity);

		response.setErrorCode("0");
		response.setSuccessMessage("Updated case record with case id : " + caseDetailsModel.getCaseId());
		return response;
	}

	/**
	 * Ported from NRCOperationServiceImpl#viewAdmission +
	 * ModelTransformationUtil#transferCaseDetailEntityToCaseDetailsRequestModel,
	 * adapted to query sub-records by caseId instead of navigating the
	 * dropped @OneToMany collections (same adaptation as
	 * DischargeService#buildDischargeRequestModel).
	 *
	 * Note: unlike viewDischarge, the source's viewAdmission does not
	 * null-check the resolved NrcUser before calling
	 * user.getHospital().getHId() - reproduced as a real null-check here
	 * instead (fails closed with a clear error) rather than letting it NPE,
	 * since a clean "Invalid user id" response is strictly better than a 500
	 * for identical input and doesn't change behavior for any valid caller.
	 */
	public AbstractResponse viewAdmission(ViewCaseRequestModel viewCaseReqModel) {
		AbstractResponse response = new AbstractResponse();

		NrcUser user = nrcUserRepository.findById(viewCaseReqModel.getLoginUserId()).orElse(null);
		if (user == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid user id provided.");
			return response;
		}
		Integer hospitalId = user.getHospitalId();

		if (viewCaseReqModel.getCaseId() == null) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("Please enter a valid case id");
			return response;
		}

		CaseDetail detailRec = caseDetailRepository.findByCaseIdAndHospitalId(viewCaseReqModel.getCaseId(), hospitalId);
		if (detailRec == null) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("The given case id does not exist");
			return response;
		}

		CaseDetailsRequestModel model = buildCaseDetailsRequestModel(detailRec);

		response.setErrorCode("0");
		response.setResponseObject(model);
		response.setSuccessMessage("Admission details for case id : " + viewCaseReqModel.getCaseId());
		return response;
	}

	private CaseDetailsRequestModel buildCaseDetailsRequestModel(CaseDetail detailRec) {
		CaseDetailsRequestModel model = new CaseDetailsRequestModel();
		Integer caseId = detailRec.getCaseId();

		if (detailRec.getUhId() != null) model.setUhId(detailRec.getUhId());
		if (detailRec.getIpNum() != null) model.setIpNum(detailRec.getIpNum());
		model.setSamNumber(detailRec.getSamNum());
		model.setAdmissionTypeId(detailRec.getAdmissionTypeId());
		if (detailRec.getAdmissionSd() != null) model.setAdmissionSd(detailRec.getAdmissionSd().intValue());
		model.setCaseId(caseId);
		model.setLoginUserId(detailRec.getCreatedBy().getuId());
		model.setHospitalId(detailRec.getCreatedBy().getHospitalId());

		var child = detailRec.getChildDemography();
		if (child.getRchId() != null) model.setRchId(child.getRchId());
		model.setChildName(child.getName());
		if (child.getDateOfBirth() != null) model.setChildDob(DateFormatUtil.getDateInyyyyMMddString(child.getDateOfBirth()));
		model.setChildAgeinYears(child.getAgeInMonths() / 12);
		model.setChildAgeinMonths(child.getAgeInMonths() % 12);
		model.setGender(genderName(child.getGender()));
		if (child.getCategory() != null) model.setCategory(child.getCategory());
		if (child.getReligonId() != null) model.setReligionId(child.getReligonId());
		if (child.getCasteId() != null) model.setCasteId(child.getCasteId());
		if (child.getMobileNumber() != null) model.setMobileNumber(child.getMobileNumber());
		if (child.getAlternateContactNumber() != null) model.setAlternateNumber(child.getAlternateContactNumber());
		if (child.getAddressLine1() != null) model.setAddressLine1(child.getAddressLine1());
		if (child.getAddressLine2() != null) model.setAddressLine2(child.getAddressLine2());
		if (child.getAddressLine3() != null) model.setAddressLine3(child.getAddressLine3());
		if (child.getPincode() != null) model.setPinCode(child.getPincode());
		if (child.getDistrictId() != null) model.setDistrictId(child.getDistrictId());

		if (detailRec.getAdmissionDate() != null) {
			model.setAdmissionDate(DateFormatUtil.getDateInyyyyMMddStringFromTimestamp(detailRec.getAdmissionDate()));
		}
		if (detailRec.getTargetWeight() != null) model.setTargetWeight(detailRec.getTargetWeight());
		model.setDischargeStatus(detailRec.getDischargeDate() != null);

		var mother = detailRec.getMotherDemogrphy();
		model.setMotherAge(mother.getAge());
		model.setMotherName(mother.getName());
		if (mother.getDateOfBirth() != null) model.setMotherDob(DateFormatUtil.getDateInyyyyMMddString(mother.getDateOfBirth()));
		if (mother.getEducationQualificationId() != null) model.setEducationQualificationId(mother.getEducationQualificationId());
		if (mother.getFamilyPlanning() != null) {
			model.setFamilyPlanning(Boolean.TRUE.equals(mother.getFamilyPlanning()) ? 1 : 2);
		}

		List<AdmissionComorbidModel> comorbidConditions = new ArrayList<>();
		for (AdmissionComorbidCondition c : admissionComorbidConditionRepository.findByCaseDetail_CaseId(caseId)) {
			AdmissionComorbidModel m = new AdmissionComorbidModel();
			m.setComorbidId(c.getComorbidId());
			m.setOthersValue(c.getOthersValue());
			m.setIsChecked(true);
			comorbidConditions.add(m);
		}
		for (var cv : comorbidConditionValueRepository.findNotYetSelectedForAdmission(caseId)) {
			AdmissionComorbidModel m = new AdmissionComorbidModel();
			m.setComorbidId(cv.getComorbidId());
			m.setIsChecked(false);
			comorbidConditions.add(m);
		}
		model.setComorbidConditions(comorbidConditions);

		List<AdmissionCriteriaModel> admissionCriterias = new ArrayList<>();
		for (AdmissionCriteria c : admissionCriteriaRepository.findByCaseDetail_CaseId(caseId)) {
			AdmissionCriteriaModel m = new AdmissionCriteriaModel();
			m.setCriteriaId(c.getCriteriaId());
			m.setOthersValue(c.getOthersValue());
			m.setIsChecked(true);
			admissionCriterias.add(m);
		}
		for (var cv : admissionCriteriaValueRepository.findNotYetSelectedForCase(caseId)) {
			AdmissionCriteriaModel m = new AdmissionCriteriaModel();
			m.setCriteriaId(cv.getCriteriaId());
			m.setIsChecked(false);
			admissionCriterias.add(m);
		}
		model.setAdmissionCriterias(admissionCriterias);

		List<CaseAllocationModel> caseAllocations = new ArrayList<>();
		CaseAllocation alloc = caseAllocationRepository.findFirstByCaseDetail_CaseId(caseId);
		if (alloc != null) {
			CaseAllocationModel m = new CaseAllocationModel();
			m.setUnitId(alloc.getUnitId());
			m.setWardId(alloc.getWardId());
			caseAllocations.add(m);
		}
		model.setCaseAllocations(caseAllocations);

		List<CaseReferralModel> caseReferrals = new ArrayList<>();
		CaseReferral referral = caseReferralRepository.findFirstByCaseDetail_CaseId(caseId);
		if (referral != null) {
			CaseReferralModel m = new CaseReferralModel();
			m.setReferredBy(referral.getReferredBy());
			m.setReferredByNumber(referral.getReferredByNumber());
			m.setReferredFromId(referral.getReferredFromId());
			caseReferrals.add(m);
		}
		model.setCaseReferrals(caseReferrals);

		List<NRCChildGrowthModel> childGrowths = new ArrayList<>();
		ChildGrowth admissionGrowth = childGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(caseId);
		if (admissionGrowth != null) {
			NRCChildGrowthModel m = new NRCChildGrowthModel();
			m.setHeightCm(admissionGrowth.getHeightCm());
			if (admissionGrowth.getMeasurementDate() != null) {
				m.setMeasurementDate(DateFormatUtil.getDateInyyyyMMddString(admissionGrowth.getMeasurementDate()));
			}
			m.setWeightKg(admissionGrowth.getWeightKg());
			childGrowths.add(m);
		}
		model.setChildGrowths(childGrowths);

		List<CaseMotherDetailModel> caseMotherDetails = new ArrayList<>();
		CaseMotherDetail cmd = caseMotherDetailRepository.findFirstByCaseDetail_CaseId(caseId);
		if (cmd != null) {
			CaseMotherDetailModel m = new CaseMotherDetailModel();
			if (cmd.getBreastFeedingAtPresent() != null) {
				m.setBreastFeedingAtPresent(Boolean.TRUE.equals(cmd.getBreastFeedingAtPresent()) ? 1 : 2);
			}
			if (cmd.getDrugsPrescribed() != null) {
				m.setDrugsPrescribed(Boolean.TRUE.equals(cmd.getDrugsPrescribed()) ? 1 : 2);
			}
			if (cmd.getMedicalProblem() != null) {
				m.setMedicalProblem(Boolean.TRUE.equals(cmd.getMedicalProblem()) ? 1 : 2);
			}
			if (cmd.getPregnantAtPresent() != null) {
				m.setPregnantAtPresent(Boolean.TRUE.equals(cmd.getPregnantAtPresent()) ? 1 : 2);
			}
			m.setNumberOfPregnancyMonths(cmd.getNumberOfPregnancyMonths());
			m.setMedicalProblemDetails(cmd.getMedicalProblemDetails());
			m.setPresentDietaryChoice(cmd.getPresentDietaryChoice());
			if (cmd.getNoOfChildren() != null) m.setNoOfChildren(cmd.getNoOfChildren());
			if (cmd.getNoOfMealsPerDay() != null) m.setNoOfMealsPerDay(cmd.getNoOfMealsPerDay());
			if (cmd.getLastMenstruationDate() != null) {
				m.setLastMenstruationDate(DateFormatUtil.getDateInyyyyMMddString(cmd.getLastMenstruationDate()));
			}
			m.setRiskCategoryId(cmd.getRiskCategoryId());
			caseMotherDetails.add(m);
		} else {
			caseMotherDetails.add(new CaseMotherDetailModel());
		}
		model.setCaseMotherDetails(caseMotherDetails);

		List<CaseMotherGrowthModel> caseMotherGrowths = new ArrayList<>();
		CaseMotherGrowth cmg = caseMotherGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(caseId);
		if (cmg != null) {
			CaseMotherGrowthModel m = new CaseMotherGrowthModel();
			m.setBloodPressureDia(cmg.getBloodPressureDia());
			m.setBloodPressureSys(cmg.getBloodPressureSys());
			m.setBloodSugarFasting(cmg.getBloodSugarFasting());
			m.setBmi(cmg.getBmi());
			if (cmg.getGravida() != null) m.setGravida(cmg.getGravida());
			if (cmg.getPara() != null) m.setPara(cmg.getPara());
			m.setWeightKg(cmg.getWeightKg());
			m.setNeckCircumferenceCm(cmg.getNeckCircumferenceCm());
			m.setMuacCm(cmg.getMuacCm());
			m.setHeightCm(cmg.getHeightCm());
			m.setHemoglobin(cmg.getHemoglobin());
			caseMotherGrowths.add(m);
		} else {
			caseMotherGrowths.add(new CaseMotherGrowthModel());
		}
		model.setCaseMotherGrowths(caseMotherGrowths);

		List<CaseMotherSocialFactorModel> caseMotherSocialFactors = new ArrayList<>();
		for (CaseMotherSocialFactor sf : caseMotherSocialFactorRepository.findByCaseDetail_CaseId(caseId)) {
			CaseMotherSocialFactorModel m = new CaseMotherSocialFactorModel();
			m.setSocialFactorsId(sf.getSocialFactorsId());
			m.setOthersValue(sf.getOthersValue());
			m.setIsChecked(true);
			caseMotherSocialFactors.add(m);
		}
		for (var sf : socialFactorRepository.findNotYetSelectedForCase(caseId)) {
			CaseMotherSocialFactorModel m = new CaseMotherSocialFactorModel();
			m.setSocialFactorsId(sf.getSocialFactorsId());
			m.setIsChecked(false);
			caseMotherSocialFactors.add(m);
		}
		model.setCaseMotherSocialFactors(caseMotherSocialFactors);

		return model;
	}

	private static String genderName(String code) {
		if ("M".equals(code)) return "Male";
		if ("F".equals(code)) return "Female";
		if ("T".equals(code)) return "Others";
		return code;
	}

	private void addComorbidData(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) {
		if (updateFlag) {
			admissionComorbidConditionRepository.deleteByCaseDetail_CaseId(caseDetailsModel.getCaseId());
		}
		if (caseDetailsModel.getComorbidConditions() == null) {
			return;
		}
		List<AdmissionComorbidCondition> list = new ArrayList<>();
		for (AdmissionComorbidModel ind : caseDetailsModel.getComorbidConditions()) {
			comorbidConditionValueRepository.findById(ind.getComorbidId()).ifPresent(comoValueObj -> {
				AdmissionComorbidCondition entity = new AdmissionComorbidCondition();
				entity.setComorbidId(comoValueObj.getComorbidId());
				entity.setOthersValue(ind.getOthersValue());
				entity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				list.add(entity);
			});
		}
		admissionComorbidConditionRepository.saveAll(list);
	}

	private void addAdmissionCriteriaData(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) {
		if (updateFlag) {
			admissionCriteriaRepository.deleteByCaseDetail_CaseId(caseDetailsModel.getCaseId());
		}
		if (caseDetailsModel.getAdmissionCriterias() == null) {
			return;
		}
		List<AdmissionCriteria> list = new ArrayList<>();
		for (AdmissionCriteriaModel ind : caseDetailsModel.getAdmissionCriterias()) {
			admissionCriteriaValueRepository.findById(ind.getCriteriaId()).ifPresent(criteriaObj -> {
				AdmissionCriteria entity = new AdmissionCriteria();
				entity.setCriteriaId(criteriaObj.getCriteriaId());
				entity.setOthersValue(ind.getOthersValue());
				entity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				list.add(entity);
			});
		}
		admissionCriteriaRepository.saveAll(list);
	}

	private void addCaseAllocData(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) {
		if (caseDetailsModel.getCaseAllocations() == null) {
			return;
		}
		List<CaseAllocation> list = new ArrayList<>();
		for (CaseAllocationModel ind : caseDetailsModel.getCaseAllocations()) {
			CaseAllocation entity = updateFlag
					? caseAllocationRepository.findFirstByCaseDetail_CaseId(caseDetailsModel.getCaseId())
					: new CaseAllocation();
			if (entity == null) {
				entity = new CaseAllocation();
			}
			CaseAllocation finalEntity = entity;
			unitRepository.findById(ind.getUnitId()).ifPresent(u -> finalEntity.setUnitId(u.getUnitId()));
			wardRepository.findById(ind.getWardId()).ifPresent(w -> finalEntity.setWardId(w.getWardId()));

			if (!updateFlag) {
				finalEntity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				finalEntity.setCreatedOn(new Timestamp(new Date().getTime()));
			} else {
				finalEntity.setLastUpdatedBy(userRef(caseDetailsModel.getLoginUserId()));
				finalEntity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			}
			finalEntity.setCaseDetail(caseDetailEntity);
			list.add(finalEntity);
		}
		caseAllocationRepository.saveAll(list);
	}

	private void addMotherDetails(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) throws NRCDateException {
		if (caseDetailsModel.getCaseMotherDetails() == null) {
			return;
		}
		List<CaseMotherDetail> list = new ArrayList<>();
		for (CaseMotherDetailModel ind : caseDetailsModel.getCaseMotherDetails()) {
			if (ind.isAllFieldsNull()) {
				continue;
			}
			CaseMotherDetail entity = updateFlag
					? caseMotherDetailRepository.findFirstByCaseDetail_CaseId(caseDetailsModel.getCaseId())
					: new CaseMotherDetail();
			boolean actuallyUpdating = updateFlag;
			if (entity == null) {
				entity = new CaseMotherDetail();
				actuallyUpdating = false;
			}
			transferCaseRequestModelToCaseMotherDetailEntity(ind, entity);

			if (ind.getLastMenstruationDate() != null) {
				try {
					entity.setLastMenstruationDate(DateFormatUtil.getDateFromyyyyMMddDashedString(ind.getLastMenstruationDate()));
				} catch (ParseException e) {
					throw new NRCDateException("Invalid date format in menstruation date");
				}
			}
			if (ind.getRiskCategoryId() != null) {
				CaseMotherDetail finalEntity = entity;
				riskCategoryRepository.findById(ind.getRiskCategoryId()).ifPresent(r -> finalEntity.setRiskCategoryId(r.getRiskCategoryId()));
			}

			if (!actuallyUpdating) {
				entity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
			} else {
				entity.setLastUpdatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			}
			entity.setCaseDetail(caseDetailEntity);
			list.add(entity);
		}
		caseMotherDetailRepository.saveAll(list);
	}

	private void addMotherGrowth(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) throws NRCDateException {
		if (caseDetailsModel.getCaseMotherGrowths() == null) {
			return;
		}
		List<CaseMotherGrowth> list = new ArrayList<>();
		for (CaseMotherGrowthModel ind : caseDetailsModel.getCaseMotherGrowths()) {
			if (ind.isAllFieldsNull()) {
				continue;
			}
			CaseMotherGrowth entity = updateFlag
					? caseMotherGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(caseDetailsModel.getCaseId())
					: new CaseMotherGrowth();
			if (entity == null) {
				entity = new CaseMotherGrowth();
			}
			transferCaseRequestModelToCaseMotherGrowthEntity(ind, entity);

			if (!updateFlag) {
				entity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
			} else {
				entity.setLastUpdatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			}
			try {
				entity.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseDetailsModel.getAdmissionDate()));
			} catch (ParseException e) {
				throw new NRCDateException("Invalid date format in mother msmt date");
			}
			entity.setCaseDetail(caseDetailEntity);
			list.add(entity);
		}
		caseMotherGrowthRepository.saveAll(list);
	}

	private void addMotherSocialFactors(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) {
		if (updateFlag) {
			caseMotherSocialFactorRepository.deleteByCaseDetail_CaseId(caseDetailsModel.getCaseId());
		}
		if (caseDetailsModel.getCaseMotherSocialFactors() == null) {
			return;
		}
		List<CaseMotherSocialFactor> list = new ArrayList<>();
		for (CaseMotherSocialFactorModel ind : caseDetailsModel.getCaseMotherSocialFactors()) {
			CaseMotherSocialFactor entity = new CaseMotherSocialFactor();
			if (ind.getSocialFactorsId() != null) {
				socialFactorRepository.findById(ind.getSocialFactorsId()).ifPresent(s -> entity.setSocialFactorsId(s.getSocialFactorsId()));
			}
			entity.setOthersValue(ind.getOthersValue());
			entity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
			entity.setCreatedOn(new Timestamp(new Date().getTime()));
			entity.setCaseDetail(caseDetailEntity);
			list.add(entity);
		}
		caseMotherSocialFactorRepository.saveAll(list);
	}

	private void addCaseReferrals(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) {
		if (caseDetailsModel.getCaseReferrals() == null) {
			return;
		}
		List<CaseReferral> list = new ArrayList<>();
		for (CaseReferralModel ind : caseDetailsModel.getCaseReferrals()) {
			CaseReferral entity = updateFlag
					? caseReferralRepository.findFirstByCaseDetail_CaseId(caseDetailsModel.getCaseId())
					: new CaseReferral();
			if (entity == null) {
				entity = new CaseReferral();
			}
			CaseReferral finalEntity = entity;
			referredFromRepository.findById(ind.getReferredFromId()).ifPresent(r -> finalEntity.setReferredFromId(r.getReferredFromId()));
			finalEntity.setReferredBy(ind.getReferredBy());
			finalEntity.setReferredByNumber(ind.getReferredByNumber());

			if (!updateFlag) {
				finalEntity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				finalEntity.setCreatedOn(new Timestamp(new Date().getTime()));
			} else {
				finalEntity.setLastUpdatedBy(userRef(caseDetailsModel.getLoginUserId()));
				finalEntity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			}
			finalEntity.setCaseDetail(caseDetailEntity);
			list.add(finalEntity);
		}
		caseReferralRepository.saveAll(list);
	}

	private void addChildGrowth(CaseDetail caseDetailEntity, CaseDetailsRequestModel caseDetailsModel, boolean updateFlag) throws NRCDateException {
		if (caseDetailsModel.getChildGrowths() == null) {
			return;
		}
		List<ChildGrowth> list = new ArrayList<>();
		for (NRCChildGrowthModel ind : caseDetailsModel.getChildGrowths()) {
			ChildGrowth entity = updateFlag
					? childGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(caseDetailsModel.getCaseId())
					: new ChildGrowth();
			if (entity == null) {
				entity = new ChildGrowth();
			}
			entity.setHeightCm(ind.getHeightCm());
			entity.setWeightKg(ind.getWeightKg());
			entity.setRecordedAtAdmission(true);
			entity.setRecordedAtDischarge(false);

			SDRequestModel sdReqModel = new SDRequestModel();
			sdReqModel.setWeight(ind.getWeightKg());
			sdReqModel.setHeight(ind.getHeightCm());
			sdReqModel.setGender(caseDetailsModel.getGender());
			String sdValue = nrcOperationService.findSDValueForCreate(sdReqModel);
			if (sdValue != null && !sdValue.isEmpty()) {
				caseDetailEntity.setAdmissionSd(new BigDecimal(sdValue));
			}

			if (!updateFlag) {
				entity.setCreatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
			} else {
				entity.setLastUpdatedBy(userRef(caseDetailsModel.getLoginUserId()));
				entity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			}
			try {
				entity.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseDetailsModel.getAdmissionDate()));
			} catch (ParseException e) {
				throw new NRCDateException("Invalid date format in measurement/admission date");
			}
			entity.setCaseDetail(caseDetailEntity);
			list.add(entity);
		}
		childGrowthRepository.saveAll(list);
	}

	// --- Field mappers, ported from nrcapp.util.ModelTransformationUtil ---

	private static void transferMotherModelToMotherEntity(CaseDetailsRequestModel m, MotherDemogrphy e, boolean updateFlag) {
		e.setName(m.getMotherName());
		e.setAge(m.getMotherAge());
		if (Integer.valueOf(1).equals(m.getFamilyPlanning())) {
			e.setFamilyPlanning(true);
		} else if (Integer.valueOf(2).equals(m.getFamilyPlanning())) {
			e.setFamilyPlanning(false);
		}
		if (!updateFlag) {
			e.setCreatedBy(userRef(m.getLoginUserId()));
			e.setCreatedOn(new Timestamp(new Date().getTime()));
		} else {
			e.setLastUpdatedBy(userRef(m.getLoginUserId()));
			e.setLastUpdatedOn(new Timestamp(new Date().getTime()));
		}
	}

	private static void transferChildModelToChildEntity(CaseDetailsRequestModel m, ChildDemography e, boolean updateFlag) {
		e.setAddressLine1(m.getAddressLine1());
		e.setAddressLine2(m.getAddressLine2());
		e.setAddressLine3(m.getAddressLine3());
		e.setPincode(m.getPinCode());
		e.setAgeInMonths(m.getChildAgeinYears() * 12 + m.getChildAgeinMonths());
		e.setCategory(m.getCategory());
		if (!updateFlag) {
			e.setCreatedBy(userRef(m.getLoginUserId()));
			e.setCreatedOn(new Timestamp(new Date().getTime()));
		} else {
			e.setLastUpdatedBy(userRef(m.getLoginUserId()));
			e.setLastUpdatedOn(new Timestamp(new Date().getTime()));
		}
		e.setGender(genderCode(m.getGender()));
		e.setMobileNumber(m.getMobileNumber());
		e.setAlternateContactNumber(m.getAlternateNumber());
		e.setName(m.getChildName());
		e.setRchId(m.getRchId());
	}

	private static void transferCaseRequestModelToCaseDetailEntity(CaseDetailsRequestModel m, CaseDetail e, boolean updateFlag) {
		Hospital hospital = new Hospital();
		hospital.setHId(m.getHospitalId());
		e.setHospital(hospital);
		e.setIpNum(m.getIpNum());
		e.setUhId(m.getUhId());
		e.setTargetWeight(m.getTargetWeight());
		if (!updateFlag) {
			e.setCreatedBy(userRef(m.getLoginUserId()));
			e.setCreatedOn(new Timestamp(new Date().getTime()));
		} else {
			e.setLastUpdatedBy(userRef(m.getLoginUserId()));
			e.setLastUpdatedOn(new Timestamp(new Date().getTime()));
		}
	}

	private static void transferCaseRequestModelToCaseMotherDetailEntity(CaseMotherDetailModel m, CaseMotherDetail e) {
		if (Integer.valueOf(1).equals(m.getBreastFeedingAtPresent())) {
			e.setBreastFeedingAtPresent(true);
		} else if (Integer.valueOf(2).equals(m.getBreastFeedingAtPresent())) {
			e.setBreastFeedingAtPresent(false);
		}
		if (Integer.valueOf(1).equals(m.getDrugsPrescribed())) {
			e.setDrugsPrescribed(true);
		} else if (Integer.valueOf(2).equals(m.getDrugsPrescribed())) {
			e.setDrugsPrescribed(false);
		}
		if (Integer.valueOf(1).equals(m.getMedicalProblem())) {
			e.setMedicalProblem(true);
		} else if (Integer.valueOf(2).equals(m.getMedicalProblem())) {
			e.setMedicalProblem(false);
		}
		e.setMedicalProblemDetails(m.getMedicalProblemDetails());
		e.setNoOfChildren(m.getNoOfChildren());
		e.setNoOfMealsPerDay(m.getNoOfMealsPerDay());
		if (Integer.valueOf(1).equals(m.getPregnantAtPresent())) {
			e.setPregnantAtPresent(true);
		} else if (Integer.valueOf(2).equals(m.getPregnantAtPresent())) {
			e.setPregnantAtPresent(false);
		}
		e.setNumberOfPregnancyMonths(m.getNumberOfPregnancyMonths());
		if (Integer.valueOf(1).equals(m.getRegisteredForAnc())) {
			e.setRegisteredForAnc(true);
		} else if (Integer.valueOf(2).equals(m.getRegisteredForAnc())) {
			e.setRegisteredForAnc(false);
		}
		e.setPresentDietaryChoice(m.getPresentDietaryChoice());
	}

	private static void transferCaseRequestModelToCaseMotherGrowthEntity(CaseMotherGrowthModel m, CaseMotherGrowth e) {
		e.setBloodPressureDia(m.getBloodPressureDia());
		e.setBloodPressureSys(m.getBloodPressureSys());
		e.setBloodSugarFasting(m.getBloodSugarFasting());
		e.setBmi(m.getBmi());
		e.setHeightCm(m.getHeightCm());
		e.setGravida(m.getGravida());
		e.setPara(m.getPara());
		e.setHemoglobin(m.getHemoglobin());
		e.setMuacCm(m.getMuacCm());
		e.setNeckCircumferenceCm(m.getNeckCircumferenceCm());
		e.setWeightKg(m.getWeightKg());
		e.setRecordedAtAdmission(true);
	}
}
