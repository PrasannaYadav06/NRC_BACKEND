package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.domain.ChildGrowth;
import com.hfw.nrc.domain.Followup;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.ChildDetailModel;
import com.hfw.nrc.dto.ChildDetailRequestModel;
import com.hfw.nrc.dto.ExistingDetailModel;
import com.hfw.nrc.dto.ExistingDetailRequestModel;
import com.hfw.nrc.dto.SearchQueryResponseModel;
import com.hfw.nrc.dto.SearchQueryResultList;
import com.hfw.nrc.dto.SearchRequestModel;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.repository.FollowupRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.util.DateFormatUtil;

/**
 * Ported from nrcapp.service.NRCOperationServiceImpl's read/search flow
 * (searchDetails/getCaseDetails/getChildDetails). Response shapes/messages
 * preserved exactly - see README "Response contract".
 */
@Service
public class CaseQueryService {

	private static final int NEW_ADMISSION = 1;
	private static final int RE_ADMISSION = 2;
	private static final int RELAPSE = 3;
	private static final long TWO_MONTH_PERIOD_DAYS = 60;

	@Autowired private NrcUserRepository nrcUserRepository;
	@Autowired private CaseDetailRepository caseDetailRepository;
	@Autowired private ChildGrowthRepository childGrowthRepository;
	@Autowired private FollowupRepository followupRepository;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Ported from NRCOperationDaoImpl#getSearchResult. The source built this
	 * WHERE clause by raw string concatenation of childName/motherName/etc
	 * directly into SQL (a real injection surface) - here the same
	 * field-priority cascade is replicated exactly, but every value is bound
	 * as a query parameter instead of concatenated.
	 */
	public AbstractResponse searchDetails(SearchRequestModel searchReqModel) {
		AbstractResponse response = new AbstractResponse();

		NrcUser user = nrcUserRepository.findById(searchReqModel.getLoginUserId()).orElse(null);
		if (user == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid user ID");
			return response;
		}
		Integer hospitalId = user.getHospitalId();

		Long uhId = searchReqModel.getUhId();
		Long rchId = searchReqModel.getRchId();
		Integer samNumber = searchReqModel.getSamNumber();
		String childName = searchReqModel.getChildName();
		String motherName = searchReqModel.getMotherName();
		String mobileNumber = searchReqModel.getMobileNumber();

		if (uhId == null && rchId == null && samNumber == null && childName == null && motherName == null && mobileNumber == null) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("No results found");
			return response;
		}

		StringBuilder where = new StringBuilder();
		List<String> orderByColumns = new ArrayList<>();
		List<Object[]> params = new ArrayList<>(); // {paramName, value}

		if (rchId != null) {
			where.append(" ch.rch_id = :rchId");
			params.add(new Object[] { "rchId", rchId });
			orderByColumns.add("child_rch_id");
		}
		if (rchId == null && uhId != null) {
			where.append(" cd.uh_id = :uhId");
			params.add(new Object[] { "uhId", uhId });
			orderByColumns.add("uh_id");
		}
		if (rchId != null && uhId != null) {
			where.append(" OR cd.uh_id = :uhId");
			params.add(new Object[] { "uhId", uhId });
			orderByColumns.add("uh_id");
		}
		if (rchId == null && uhId == null && samNumber != null) {
			where.append(" cd.sam_num = :samNumber");
			params.add(new Object[] { "samNumber", samNumber });
			orderByColumns.add("sam_number");
		}
		if (rchId != null && uhId != null && samNumber != null) {
			where.append(" OR cd.sam_num = :samNumber");
			params.add(new Object[] { "samNumber", samNumber });
			orderByColumns.add("sam_number");
		}
		if (rchId == null && uhId == null && samNumber == null && childName != null) {
			where.append(" ch.name like :childName");
			params.add(new Object[] { "childName", childName + "%" });
			orderByColumns.add("child_name");
		}
		if (rchId != null && uhId != null && samNumber != null && childName != null) {
			where.append(" OR ch.name like :childName");
			params.add(new Object[] { "childName", childName + "%" });
			orderByColumns.add("child_name");
		}
		if (rchId == null && uhId == null && samNumber == null && childName == null && motherName != null) {
			where.append(" mr.name like :motherName");
			params.add(new Object[] { "motherName", motherName + "%" });
			orderByColumns.add("mother_name");
		}
		if (rchId != null && uhId != null && samNumber != null && childName != null && motherName != null) {
			where.append(" OR mr.name like :motherName");
			params.add(new Object[] { "motherName", motherName + "%" });
			orderByColumns.add("mother_name");
		}
		if (rchId == null && uhId == null && samNumber == null && childName == null && motherName == null
				&& mobileNumber != null && mobileNumber.length() == 10) {
			where.append(" ch.mobile_number = :mobileNumber");
			params.add(new Object[] { "mobileNumber", mobileNumber });
			orderByColumns.add("mobile_number");
		}
		if (rchId != null && uhId != null && samNumber != null && childName != null && motherName != null
				&& mobileNumber != null && mobileNumber.length() == 10) {
			where.append(" OR ch.mobile_number = :mobileNumber");
			params.add(new Object[] { "mobileNumber", mobileNumber });
			orderByColumns.add("mobile_number");
		}

		if (where.length() == 0) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("No results found");
			return response;
		}

		boolean inTrackChild = Boolean.TRUE.equals(searchReqModel.getInTrackChild());
		String hql2 = inTrackChild
				? where + " and cd.h_id = :hospitalId) select * from cte where rn = 1 order by "
				: where + " and cd.discharge_date is not null and cd.h_id = :hospitalId) select * from cte where rn = 1 order by ";

		String hql1 = "with cte as (select cd.uh_id, ch.rch_id as child_rch_id, cd.sam_num as sam_number, ch.name as child_name, "
				+ "mr.name as mother_name, ch.mobile_number, cd.admission_date, cd.discharge_date, cd.case_id, cd.h_id, "
				+ "ROW_NUMBER() over (partition by ch.child_id order by cd.admission_date desc) as rn "
				+ "from nr.case_details cd inner join nr.child_demography ch on cd.child_id = ch.child_id "
				+ "inner join nr.mother_demogrphy mr on mr.mother_id = cd.mother_id where";

		String orderByFields = String.join(",", orderByColumns);
		String hql = hql1 + hql2 + orderByFields + ", admission_date desc, discharge_date desc";

		Query query = entityManager.createNativeQuery(hql);
		for (Object[] p : params) {
			query.setParameter((String) p[0], p[1]);
		}
		query.setParameter("hospitalId", hospitalId);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = query.getResultList();

		if (rows.isEmpty()) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("No search results found");
			return response;
		}

		SearchQueryResultList resultList = new SearchQueryResultList();
		for (Object[] row : rows) {
			SearchQueryResponseModel sqTemp = new SearchQueryResponseModel();
			if (row[0] != null) sqTemp.setUhId(((Number) row[0]).longValue());
			if (row[1] != null) sqTemp.setRchId(((Number) row[1]).longValue());
			if (row[2] != null) sqTemp.setSamNumber(((Number) row[2]).intValue());
			if (row[3] != null) sqTemp.setChildName(String.valueOf(row[3]));
			if (row[4] != null) sqTemp.setMotherName(String.valueOf(row[4]));
			if (row[5] != null) sqTemp.setMobileNumber(String.valueOf(row[5]));
			if (row[6] != null) {
				sqTemp.setLastAdmittedDate(DateFormatUtil.getDateInddMMyyyyhhMMssStringFromTimestamp((java.sql.Timestamp) row[6]));
			}
			if (row[7] != null) {
				sqTemp.setLastDischargeDate(DateFormatUtil.getDateInddMMyyyyhhMMssStringFromTimestamp((java.sql.Timestamp) row[7]));
			}
			Integer caseId = row[8] != null ? ((Number) row[8]).intValue() : null;
			Integer rowHospitalId = row[9] != null ? ((Number) row[9]).intValue() : null;
			sqTemp.setCaseId(caseId);
			sqTemp.setHospitalId(rowHospitalId);

			if (caseId != null && rowHospitalId != null) {
				CaseDetail cs = caseDetailRepository.findByCaseIdAndHospitalId(caseId, rowHospitalId);
				if (cs != null) {
					sqTemp.setGender(cs.getChildDemography().getGender());
				}
				Followup lastFollowup = followupRepository.findLastForCase(caseId);
				if (lastFollowup != null && lastFollowup.getFollowupDate() != null) {
					sqTemp.setLastFollowupDate(DateFormatUtil.getDateInddMMyyyyString(lastFollowup.getFollowupDate()));
				}
			}

			Date startDate = null;
			if (sqTemp.getLastDischargeDate() != null) {
				try {
					startDate = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(sqTemp.getLastDischargeDate().substring(0, 10));
				} catch (ParseException ignored) {
				}
			}
			if (startDate == null) {
				sqTemp.setAdmissionTypeId(NEW_ADMISSION);
			} else {
				long diffInDays = (new Date().getTime() - startDate.getTime()) / (1000L * 60 * 60 * 24);
				sqTemp.setAdmissionTypeId(diffInDays <= TWO_MONTH_PERIOD_DAYS ? RE_ADMISSION : RELAPSE);
			}

			resultList.getSearchResultList().add(sqTemp);
		}

		response.setErrorCode("0");
		response.setResponseObject(resultList);
		response.setSuccessMessage("Search results found for the given parameters");
		return response;
	}

	public AbstractResponse getCaseDetails(ExistingDetailRequestModel edrModel) {
		AbstractResponse res = new AbstractResponse();
		CaseDetail detailRec = caseDetailRepository.findByCaseIdAndHospitalId(edrModel.getCaseId(), edrModel.getHospitalId());

		if (detailRec == null) {
			res.setErrorCode("1");
			res.setErrorMessage("No details found for the given caseId :" + edrModel.getCaseId());
			res.setResponseObject(null);
			return res;
		}

		ExistingDetailModel edResModel = new ExistingDetailModel();
		if (detailRec.getUhId() != null) edResModel.setUhId(detailRec.getUhId());
		edResModel.setSamNumber(detailRec.getSamNum());
		var child = detailRec.getChildDemography();
		if (child.getAddressLine1() != null) edResModel.setAddressLine1(child.getAddressLine1());
		if (child.getAddressLine2() != null) edResModel.setAddressLine2(child.getAddressLine2());
		if (child.getAddressLine3() != null) edResModel.setAddressLine3(child.getAddressLine3());
		if (child.getPincode() != null) edResModel.setPincode(child.getPincode());
		if (child.getDistrictId() != null) edResModel.setDistrictId(child.getDistrictId());
		if (child.getCasteId() != null) edResModel.setCasteId(child.getCasteId());
		if (child.getCategory() != null) edResModel.setCategory(child.getCategory());
		edResModel.setChildAgeinYears(child.getAgeInMonths() / 12);
		edResModel.setChildAgeinMonths(child.getAgeInMonths() % 12);
		if (child.getDateOfBirth() != null) edResModel.setChildDob(DateFormatUtil.getDateInyyyyMMddString(child.getDateOfBirth()));
		edResModel.setGender(genderName(child.getGender()));
		if (child.getMobileNumber() != null) edResModel.setMobileNumber(child.getMobileNumber());
		edResModel.setChildName(child.getName());
		if (child.getRchId() != null) edResModel.setRchId(child.getRchId());
		if (child.getReligonId() != null) edResModel.setReligionId(child.getReligonId());
		var mother = detailRec.getMotherDemogrphy();
		edResModel.setMotherAge(mother.getAge());
		if (mother.getDateOfBirth() != null) edResModel.setMotherDob(DateFormatUtil.getDateInyyyyMMddString(mother.getDateOfBirth()));
		if (mother.getName() != null) edResModel.setMotherName(mother.getName());

		if (detailRec.getDischargeDate() != null) {
			long diffInDays = (new Date().getTime() - detailRec.getDischargeDate().getTime()) / (1000L * 60 * 60 * 24);
			edResModel.setAdmissionTypeId(diffInDays <= TWO_MONTH_PERIOD_DAYS ? RE_ADMISSION : RELAPSE);
		} else {
			edResModel.setAdmissionTypeId(NEW_ADMISSION);
		}

		res.setErrorCode("0");
		res.setSuccessMessage("Found case detail for caseId: " + edrModel.getCaseId());
		res.setResponseObject(edResModel);
		return res;
	}

	public AbstractResponse getChildDetails(ChildDetailRequestModel cdrModel) {
		AbstractResponse res = new AbstractResponse();
		CaseDetail detailRec = caseDetailRepository.findByCaseIdAndHospitalId(cdrModel.getCaseId(), cdrModel.getHospitalId());
		if (detailRec == null) {
			res.setErrorCode("1");
			res.setErrorMessage("No child  details found for case id: " + cdrModel.getCaseId());
			return res;
		}

		ChildGrowth growthRec = childGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(cdrModel.getCaseId());
		if (growthRec == null) {
			res.setErrorCode("1");
			res.setErrorMessage("No child  details found for case id: " + cdrModel.getCaseId());
			return res;
		}

		ChildDetailModel childModel = new ChildDetailModel();
		var child = detailRec.getChildDemography();
		childModel.setChildName(child.getName());
		childModel.setGender(child.getGender());
		if (child.getMobileNumber() != null) childModel.setMobileNumber(child.getMobileNumber());
		if (child.getRchId() != null) childModel.setRchId(child.getRchId());
		if (detailRec.getIpNum() != null) childModel.setIpNumber(detailRec.getIpNum());
		childModel.setSamNumber(detailRec.getSamNum());
		if (detailRec.getUhId() != null) childModel.setUhId(detailRec.getUhId());
		childModel.setCaseId(detailRec.getCaseId());
		if (detailRec.getAdmissionDate() != null) {
			childModel.setAdmissionDate(DateFormatUtil.getDateInyyyyMMddStringFromTimestamp(detailRec.getAdmissionDate()));
		}
		if (detailRec.getAdmissionSd() != null) {
			childModel.setAdmissionSd(detailRec.getAdmissionSd());
		}

		BigDecimal admissionWeight = growthRec.getWeightKg();
		BigDecimal minWeight = admissionWeight;
		Date minWeightDate = growthRec.getMeasurementDate();

		List<ChildGrowth> growthList = childGrowthRepository.findByCaseDetail_CaseIdOrderByMeasurementDateDesc(detailRec.getCaseId());
		if (growthList.size() > 1) {
			List<ChildGrowth> ascending = new ArrayList<>(growthList);
			Collections.sort(ascending, (g1, g2) -> g1.getMeasurementDate().compareTo(g2.getMeasurementDate()));
			for (ChildGrowth cg : ascending) {
				childModel.setDischargeWeight(cg.getWeightKg());
				if (admissionWeight != null && cg.getWeightKg() != null && cg.getWeightKg().compareTo(admissionWeight) < 0) {
					if (cg.getWeightKg().compareTo(minWeight) < 0) {
						minWeight = cg.getWeightKg();
						minWeightDate = cg.getMeasurementDate();
					}
				}
			}
		}

		childModel.setMinWeight(minWeight);
		if (minWeightDate != null) {
			childModel.setMinWeightDate(DateFormatUtil.getDateInyyyyMMddString(minWeightDate));
		}
		childModel.setAdmissionHeight(growthRec.getHeightCm());
		childModel.setAdmissionWeight(growthRec.getWeightKg());
		childModel.setDischargeHeight(growthRec.getHeightCm());

		res.setErrorCode("0");
		res.setSuccessMessage("Found child details requested for case id : " + cdrModel.getCaseId());
		res.setResponseObject(childModel);
		return res;
	}

	private static String genderName(String code) {
		if ("M".equals(code)) return "Male";
		if ("F".equals(code)) return "Female";
		if ("T".equals(code)) return "Others";
		return code;
	}
}
