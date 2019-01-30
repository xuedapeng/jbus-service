package com.moqbus.service.db.mysql.bean;

import com.moqbus.service.db.mysql.cache.CacheableEntity;

public class DeviceEntity extends CacheableEntity {

	private Integer id;
	private String deviceSn;
	private String deviceName;

	


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




	public String getDeviceName() {
		return deviceName;
	}




	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}




	@Override
	public String getCacheKeyVal() {
		
		return deviceSn;
	}
	
}
