package com.moqbus.service.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CacheProxy<E, D> {

	public List<E> list = new ArrayList<E>();
	public Map<String, E> map = new HashMap<String, E>();
	public Map<String, Map<String, Object>> attrMap = new HashMap<String, Map<String, Object>>();
	
	int _updateInterval = 10*1000; // 默认10秒检查更新
	String _cacheName = "";

	public Long maxId = 0L;
	public Date maxTime = new Date();
	
	public CacheProxy(String cacheName) {
		_cacheName = cacheName;
		load();
		makeMap();
		run();
	}
	
	
	public Map<String, Object> getAttribute(String attrName) {
		if (!attrMap.containsKey(attrName)) {
			attrMap.put(attrName, new HashMap<String, Object>());
		}
		return attrMap.get(attrName);
	}
	
	public abstract void load();
	public abstract boolean update();
	public abstract void makeMap();
	
	private void run() {
		new Thread() {
			
			@Override
			public void run() {
				while(true) {
					if (update()) {
						makeMap();
					}
				}
			}
		}.start();
	}
	
	
}
