package com.moqbus.service.db.mysql.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.moqbus.service.db.mysql.bean.WarningContactEntity;

public class TableCache<E extends CacheableEntity, D extends CacheableDao<E>> {

	static Logger log = Logger.getLogger(TableCache.class);

	public List<E> list = new LinkedList<E>();
	public Map<String, E> map = new HashMap<String, E>();
	public Map<String, Map<String, Object>> attrMap = new HashMap<String, Map<String, Object>>();
	
	int _updateInterval = 10*1000; // 默认10秒检查更新
	String _cacheName = "";
	CacheableDao<E> _dao;

	Integer _maxId = 0;
	Date _maxTime = new Date();
	
	public TableCache(String cacheName, CacheableDao<E> dao, int updateInterval) {

		_dao = dao;
		_cacheName = cacheName;
		_updateInterval = updateInterval*1000;
		load();
		run();
	}
	
	
	public Map<String, Object> getAttribute(String attrName) {
		if (!attrMap.containsKey(attrName)) {
			attrMap.put(attrName, new HashMap<String, Object>());
		}
		return attrMap.get(attrName);
	}

	private void run() {
		new Thread() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(_updateInterval);
						update();
					} catch (InterruptedException e) {
						log.error("", e);
					}
				}
			}
		}.start();
	}
	
	public void load() {

		_maxTime = new Date();
		_maxId = _dao.findMaxId();
		list = (List<E>) _dao.findAll();
		makeMap();
		log.info(_cacheName + " load:list.size=" +list.size());
			
	}

	
	public synchronized boolean update() {

		boolean ret = false;
		if (list.isEmpty()) {
			load();
			return true;
		}
		
		boolean hasRemove = false;
		Long countTimeChanged = _dao.countAfterTime(_maxTime);
		Long countAll = _dao.countAll();
		Date preMaxTime = _maxTime;
		Integer preMaxId = _maxId;
		Integer preListSize = list.size();
		_maxTime = new Date();
		_maxId = _dao.findMaxId();
		int addedCount = 0;
		
		if (countTimeChanged > 0) {
			ret = true;
			List<E> listTimeChanged = (List<E>) _dao.findAfterTime(preMaxTime);
			
			List<E> remove4updateList = new ArrayList<E>();
			list.forEach((item)->{
				listTimeChanged.forEach((changedItem)->{

					if (item.getId().equals(changedItem.getId())) {
						remove4updateList.add(item);
					}
				});
			});
			
			for(E e: listTimeChanged) {
				
				boolean contains = list.contains(e);
				
				if(e.getId() > preMaxId || !contains) {
					addedCount++;
				}
				list.add(e);
				if (e instanceof WarningContactEntity) {
					log.info("AAAAAAAAA e.email=" + ((WarningContactEntity)e).getEmail());
				}
			}

			remove4updateList.forEach((item)->{
				list.remove(item);
			});
			
			log.info(String.format("Cache->%s, added:%d", _cacheName, addedCount));
			log.info(String.format("Cache->%s, updated:%d", _cacheName, listTimeChanged.size() - addedCount));
			
		}

		if (preListSize+ addedCount > countAll) {
			hasRemove = true;
			ret = true;
		}
		
		if (hasRemove) {
			List<Integer> idsList = _dao.findAllIds();
			List<E> removeList = new ArrayList<E>();
			list.forEach((item)->{
				if (!idsList.contains(item.getId())) {
					removeList.add(item);
				}
			});
			
			removeList.forEach((item)->{
				list.remove(item);
				
			});
			
			log.info(String.format("Cache->%s, removed:%d", _cacheName, removeList.size()));
		}
		if (ret) {
			makeMap();
		}

		log.info(String.format("Cache->%s, list.size= %d", _cacheName, list.size()));
		return ret;
	}

	private void makeMap() {
		
		if (!list.isEmpty()) {

			log.info(String.format("Cache->%s, list.size= %d", _cacheName, list.size()));
			map = list.stream().collect(Collectors.toMap(E::getCacheKeyVal, a -> a,(k1,k2)->k1));

			log.info(String.format("Cache->%s, map.size= %d", _cacheName, map.size()));
			if (_cacheName.indexOf("Contact")>0) {
				log.info(String.format("BBBBBBBB Cache->%s, map->BJW401= %s", _cacheName, ((WarningContactEntity)map.get("BJW401")).getEmail()));
			}
			}
	}
	

//	 private Class<E> getClassE() {
//        @SuppressWarnings("unchecked")
//		Class<E> eClass = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        return eClass;
//    }
	 
//	 private Class<D> getClassD() {
//        ParameterizedType parameterizedType=(ParameterizedType)this.getClass().getGenericSuperclass();
//		Class<D> dClass = (Class<D>)parameterizedType.getActualTypeArguments()[1];
//        return dClass;
//	 }

}
