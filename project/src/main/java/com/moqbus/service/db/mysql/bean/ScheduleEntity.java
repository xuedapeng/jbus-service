package com.moqbus.service.db.mysql.bean;

import java.util.Date;

import com.moqbus.service.db.mysql.cache.CacheableEntity;

public class ScheduleEntity extends CacheableEntity {
	
	private Integer id;
	private Integer deviceId;
	private String deviceSn;
//	private Integer sno;
	private String cmdHex;
	private String datPtn;
	private Integer interval;
	private Integer status;
	private Integer dataLimit;
	private Integer delay;
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
	
//	public Integer getSno() {
//		return sno;
//	}
//	public void setSno(Integer sno) {
//		this.sno = sno;
//	}
	public String getDatPtn() {
		return datPtn;
	}
	public void setDatPtn(String datPtn) {
		this.datPtn = datPtn;
	}
	
	
	public Integer getDataLimit() {
		return dataLimit;
	}
	public void setDataLimit(Integer dataLimit) {
		this.dataLimit = dataLimit;
	}
	
	
	
	public Integer getDelay() {
		return delay;
	}
	public void setDelay(Integer delay) {
		this.delay = delay;
	}
	@Override
	public String getCacheKeyVal() {
		
		return deviceSn + "_" + id;
	}
	
	
	
}
	