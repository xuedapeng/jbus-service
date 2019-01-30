package com.moqbus.service.codec;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.moqbus.service.application.Global;
import com.moqbus.service.db.mysql.cache.TableCache;

public class ParseCache {

	final static int MAX_PER_DEVICE = 10;
	// <deviceSn, <data, result>]>
	static Map<String, List<CacheObject>> _cache = new ConcurrentHashMap<String, List<CacheObject>>();
	static {
		Global.cacheDatDecode.addMonitor(TableCache.ChangeType.UPDATE, (deviceSn,decode)->{
			_cache.remove(deviceSn);
		});
		Global.cacheDatDecode.addMonitor(TableCache.ChangeType.DELETE, (deviceSn,decode)->{
			_cache.remove(deviceSn);
		});
	}
	
	public static void put(String deviceSn, String data, String result) {
		
		List<CacheObject> cacheList = _cache.get(deviceSn);
		if (cacheList ==null) {
			cacheList = new LinkedList<CacheObject>();
			_cache.put(deviceSn, cacheList);
		}
		
		cacheList.add(0, new CacheObject(data, result));
		
		// 删除最旧的缓存
		while (cacheList.size() > MAX_PER_DEVICE) {
			cacheList.remove(cacheList.size()-1);
		}
		
	}

	public static String get(String deviceSn, String data) {
		
		if (!contains(deviceSn, data)) {

			return null;
		}
		
		List<CacheObject> list = _cache.get(deviceSn);
		CacheObject co = CacheObject.getByData(list, data);
		
		// 调整顺序
		if (list.remove(co)) {
			list.add(0, co);
		}
		
		return co.result();
		
	}

	public static boolean contains(String deviceSn, String data) {
		
		if(_cache.containsKey(deviceSn)) {
			if (_cache.get(deviceSn).contains(new CacheObject(data, ""))) {
				return  true;
			}
		}
		
		return false;
	}
	
	public static class CacheObject {
		private String[] _item = {"", ""};
		
		public CacheObject(String data, String result) {
			_item[0] = data;
			_item[1] = result;
		}
		
		public String data() {
			return _item[0];
			
		}

		public String result() {
			return _item[1];
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof CacheObject) {
				return data().equals(((CacheObject)o).data());
			}
			return false;
		}
		
		public static CacheObject getByData(List<CacheObject> list, String data) {
			for(CacheObject o: list) {
				if (o.data().equals(data)) {
					return o;
				}
			}
			
			return null;
		}
		
	}
	
	
}
