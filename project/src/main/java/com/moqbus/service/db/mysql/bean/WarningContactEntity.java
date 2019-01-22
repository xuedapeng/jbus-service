package com.moqbus.service.db.mysql.bean;

import java.util.Date;

import com.moqbus.service.db.mysql.cache.CacheableEntity;

public class WarningContactEntity extends CacheableEntity {

	private Integer id;
	private String deviceSn;
	private String email;
	private Integer event;
	private Integer includeCrc;
	private Integer status;
	private Date updateTime;
	
	


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




	public String getEmail() {
		return email;
	}




	public void setEmail(String email) {
		this.email = email;
	}




	public Integer getEvent() {
		return event;
	}




	public void setEvent(Integer event) {
		this.event = event;
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
