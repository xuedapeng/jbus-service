package com.moqbus.service.db.mysql.bean;

import java.util.Date;

import com.moqbus.service.db.mysql.cache.CacheableEntity;

public class ScheduleEntity extends CacheableEntity {
	
	private Integer id;
	private Integer deviceId;
	private String deviceSn;
	private String cmdHex;
	private Integer interval;
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
	public String getCmdHex() {
		return cmdHex;
	}
	public void setCmdHex(String cmdHex) {
		this.cmdHex = cmdHex;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
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
	public Integer getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}
	@Override
	public String getCacheKeyVal() {
		
		return deviceSn;
	}
	
}
	