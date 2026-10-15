package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Infrastructure Module photo gallery (V35) - many rows per hospital. filePath is relative to nrc.uploads.dir (application.properties), never an absolute path. */
@Entity
@Table(name = "nrc_infrastructure_photo", schema = "nr")
public class NrcInfrastructurePhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "photo_id")
	private int photoId;

	@Column(name = "h_id")
	private int hId;

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "caption")
	private String caption;

	@Column(name = "uploaded_on")
	private Timestamp uploadedOn;

	@Column(name = "uploaded_by")
	private Integer uploadedBy;

	public int getPhotoId() {
		return photoId;
	}

	public void setPhotoId(int photoId) {
		this.photoId = photoId;
	}

	public int getHId() {
		return hId;
	}

	public void setHId(int hId) {
		this.hId = hId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Timestamp getUploadedOn() {
		return uploadedOn;
	}

	public void setUploadedOn(Timestamp uploadedOn) {
		this.uploadedOn = uploadedOn;
	}

	public Integer getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(Integer uploadedBy) {
		this.uploadedBy = uploadedBy;
	}
}
