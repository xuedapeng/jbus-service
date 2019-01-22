package com.moqbus.service.db.mysql.bean;

import java.util.Date;

import com.moqbus.service.db.mysql.cache.CacheableEntity;

public class DatDecodeEntity extends CacheableEntity {

	private Integer id;
	private Integer deviceId;
	private String deviceSn;
	private String scriptText;
	private String resultSchema;
	private Integer includeCrc;
	private Integer status;
	private Date updateTime;
	
	
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}


	public String getDeviceSn() {
		return deviceSn;
	}


	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}


	public String getScriptText() {
		return scriptText;
	}


	public void setScriptText(String scriptText) {
		this.scriptText = scriptText;
	}


	public String getResultSchema() {
		return resultSchema;
	}


	public void setResultSchema(String resultSchema) {
		this.resultSchema = resultSchema;
	}


	public Integer getIncludeCrc() {
		return includeCrc;
	}


	public void setIncludeCrc(Integer includeCrc) {
		this.includeCrc = includeCrc;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Date getUpdateTime() {
		return updateTime;
	}


	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}


	@Override
	public String getCacheKeyVal() {
		
		return deviceSn;
	}
	
}
