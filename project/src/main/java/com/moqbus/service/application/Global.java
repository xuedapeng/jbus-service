package com.moqbus.service.application;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.moqbus.service.common.conf.ZSystemConfig;
import com.moqbus.service.db.mysql.bean.DatDecodeEntity;
import com.moqbus.service.db.mysql.bean.DeviceEntity;
import com.moqbus.service.db.mysql.bean.ScheduleEntity;
import com.moqbus.service.db.mysql.bean.WarningContactEntity;
import com.moqbus.service.db.mysql.cache.TableCache;
import com.moqbus.service.db.mysql.dao.DatDecodeDao;
import com.moqbus.service.db.mysql.dao.DeviceDao;
import com.moqbus.service.db.mysql.dao.ScheduleDao;
import com.moqbus.service.db.mysql.dao.WarningContactDao;
import com.moqbus.service.proxy.ThreadProxy;

public class Global {
	
	public static void config() {

		// log4j
		String log4jConfig = ZSystemConfig.getProperty("log4j.config");
		if (log4jConfig != null) {
			if (log4jConfig.indexOf("/") < 0) {
				log4jConfig =  new File(ZSystemConfig.getSystemConfigPath(),log4jConfig).getAbsolutePath();
			}

			PropertyConfigurator.configure(log4jConfig);  
		}
	}
	

	public static ThreadProxy threadProxySend = new ThreadProxy(5, 10, "threadProxySend");
	public static ThreadProxy threadProxyRecv = new ThreadProxy(10, 1000, "threadProxyRecv");
	public static ThreadProxy threadProxyEvent = new ThreadProxy(5, 1000, "threadProxyEvent");
	public static ThreadProxy threadProxyWarning = new ThreadProxy(5, 1000, "threadProxyWarning");
	
	public static TableCache<ScheduleEntity, ScheduleDao<ScheduleEntity>> cacheSchedule = 
			new TableCache<ScheduleEntity, ScheduleDao<ScheduleEntity>>(
					"scheduleCache", new ScheduleDao<ScheduleEntity>(), 30);
	public static TableCache<DatDecodeEntity, DatDecodeDao<DatDecodeEntity>> cacheDatDecode = 
			new TableCache<DatDecodeEntity, DatDecodeDao<DatDecodeEntity>>(
					"datDecodeCache", new DatDecodeDao<DatDecodeEntity>(), 30);
	public static TableCache<WarningContactEntity, WarningContactDao<WarningContactEntity>> cacheWarningContact = 
			new TableCache<WarningContactEntity, WarningContactDao<WarningContactEntity>>(
					"warningContactCache", new WarningContactDao<WarningContactEntity>(), 30);
	public static TableCache<DeviceEntity, DeviceDao<DeviceEntity>> cacheDevice = 
			new TableCache<DeviceEntity, DeviceDao<DeviceEntity>>(
					"deviceCache", new DeviceDao<DeviceEntity>(), 30);


}
