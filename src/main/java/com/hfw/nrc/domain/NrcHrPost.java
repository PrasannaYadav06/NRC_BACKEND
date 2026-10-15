package com.hfw.nrc.domain;

import java.sql.Date;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stage 6 (revised) - one row per HR post-slot submission (a hire, or a
 * later resignation/vacate update against that same slot), ported from
 * KABHI's KabhiHrEntity/tbl_kabhi_hr. The *current* state of a post is the
 * latest row for the same (hId, designation, slotNo) that has
 * approvalStatus = APPROVED and isCurrent = true; every other row for that
 * slot is retained as history. A row with approvalStatus = PENDING is an
 * in-flight submission awaiting admin approval and does not affect the
 * post's current state yet - see NrcHrPostService for the full state
 * machine. See db/migration/V17 for the one deviation from KABHI (sanctioned
 * counts are per-hospital here, not global-per-designation).
 */
@Entity
@Table(name = "nrc_hr_post", schema = "nr")
public class NrcHrPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hr_post_id")
	private Integer hrPostId;

	@Column(name = "h_id")
	private int hId;

	@Column(name = "designation")
	private String designation;

	@Column(name = "slot_no")
	private int slotNo;

	@Column(name = "person_name")
	private String personName;

	@Column(name = "date_of_joining")
	private Date dateOfJoining;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "contact_no")
	private String contactNo;

	@Column(name = "employment_status")
	private String employmentStatus;

	/** Comma-separated day names, only populated when employmentStatus = On-Consultation. */
	@Column(name = "days_of_consultation")
	private String daysOfConsultation;

	/** PENDING / APPROVED / REJECTED. */
	@Column(name = "approval_status")
	private String approvalStatus;

	/** True only for the single row that represents the slot's live/approved state. */
	@Column(name = "is_current")
	private boolean isCurrent;

	@Column(name = "submitted_by")
	private String submittedBy;

	@Column(name = "submitted_date")
	private LocalDateTime submittedDate;

	@Column(name = "approved_by")
	private String approvedBy;

	@Column(name = "approved_date")
	private LocalDateTime approvedDate;

	@Column(name = "remarks")
	private String remarks;

	// RCHO review tier (V26) - PENDING -> RCHO_APPROVED (these columns) -> APPROVED (approvedBy/approvedDate/remarks above, the final admin tier). A REJECT at either tier is terminal REJECTED; which of these vs the admin-tier columns is populated records which tier acted.
	@Column(name = "rcho_approved_by")
	private String rchoApprovedBy;

	@Column(name = "rcho_approved_date")
	private LocalDateTime rchoApprovedDate;

	@Column(name = "rcho_remarks")
	private String rchoRemarks;

	public String getRchoApprovedBy() { return rchoApprovedBy; }
	public void setRchoApprovedBy(String rchoApprovedBy) { this.rchoApprovedBy = rchoApprovedBy; }
	public LocalDateTime getRchoApprovedDate() { return rchoApprovedDate; }
	public void setRchoApprovedDate(LocalDateTime rchoApprovedDate) { this.rchoApprovedDate = rchoApprovedDate; }
	public String getRchoRemarks() { return rchoRemarks; }
	public void setRchoRemarks(String rchoRemarks) { this.rchoRemarks = rchoRemarks; }

	public Integer getHrPostId() { return hrPostId; }
	public void setHrPostId(Integer hrPostId) { this.hrPostId = hrPostId; }
	public int getHId() { return hId; }
	public void setHId(int hId) { this.hId = hId; }
	public String getDesignation() { return designation; }
	public void setDesignation(String designation) { this.designation = designation; }
	public int getSlotNo() { return slotNo; }
	public void setSlotNo(int slotNo) { this.slotNo = slotNo; }
	public String getPersonName() { return personName; }
	public void setPersonName(String personName) { this.personName = personName; }
	public Date getDateOfJoining() { return dateOfJoining; }
	public void setDateOfJoining(Date dateOfJoining) { this.dateOfJoining = dateOfJoining; }
	public String getEmailId() { return emailId; }
	public void setEmailId(String emailId) { this.emailId = emailId; }
	public String getContactNo() { return contactNo; }
	public void setContactNo(String contactNo) { this.contactNo = contactNo; }
	public String getEmploymentStatus() { return employmentStatus; }
	public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
	public String getDaysOfConsultation() { return daysOfConsultation; }
	public void setDaysOfConsultation(String daysOfConsultation) { this.daysOfConsultation = daysOfConsultation; }
	public String getApprovalStatus() { return approvalStatus; }
	public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
	public boolean isCurrent() { return isCurrent; }
	public void setCurrent(boolean current) { isCurrent = current; }
	public String getSubmittedBy() { return submittedBy; }
	public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
	public LocalDateTime getSubmittedDate() { return submittedDate; }
	public void setSubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }
	public String getApprovedBy() { return approvedBy; }
	public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
	public LocalDateTime getApprovedDate() { return approvedDate; }
	public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
	public String getRemarks() { return remarks; }
	public void setRemarks(String remarks) { this.remarks = remarks; }
}
