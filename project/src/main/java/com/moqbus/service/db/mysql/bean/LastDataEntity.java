package com.moqbus.service.db.mysql.bean;

import java.util.Date;

public class LastDataEntity {

	private Integer id;
	private String deviceSn;
	private String sensorNo;
	private String dsKey;
	private String message;
	private Date createTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDeviceSn() {
		return deviceSn;
	}
	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}
	public String getSensorNo() {
		return sensorNo;
	}
	public void setSensorNo(String sensorNo) {
		this.sensorNo = sensorNo;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getDsKey() {
		return dsKey;
	}
	public void setDsKey(String dsKey) {
		this.dsKey = dsKey;
	}
	
	
	
}
