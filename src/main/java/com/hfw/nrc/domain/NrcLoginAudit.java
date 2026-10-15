package com.hfw.nrc.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** See db/migration/V3__add_login_security_columns.sql - append-only login/logout audit log. */
@Entity
@Table(name = "nrc_login_audit", schema = "nr")
public class NrcLoginAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "audit_id")
	private int auditId;

	@Column(name = "u_id")
	private Integer uId;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "event_type")
	private String eventType;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "event_on")
	private Timestamp eventOn;

	public int getAuditId() { return auditId; }
	public void setAuditId(int auditId) { this.auditId = auditId; }

	public Integer getuId() { return uId; }
	public void setuId(Integer uId) { this.uId = uId; }

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public String getEventType() { return eventType; }
	public void setEventType(String eventType) { this.eventType = eventType; }

	public String getIpAddress() { return ipAddress; }
	public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

	public Timestamp getEventOn() { return eventOn; }
	public void setEventOn(Timestamp eventOn) { this.eventOn = eventOn; }
}
