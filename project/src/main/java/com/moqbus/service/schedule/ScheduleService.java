package com.moqbus.service.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.moqbus.service.application.Global;
import com.moqbus.service.codec.NashornParser;
import com.moqbus.service.common.constant.JbusConst;
import com.moqbus.service.common.exception.JbusException;
import com.moqbus.service.common.helper.ByteHelper;
import com.moqbus.service.common.helper.DateHelper;
import com.moqbus.service.common.helper.HexHelper;
import com.moqbus.service.common.helper.JsonHelper;
import com.moqbus.service.db.mysql.bean.DatDecodeEntity;
import com.moqbus.service.db.mysql.bean.ScheduleEntity;
import com.moqbus.service.db.mysql.cache.TableCache.ChangeType;
import com.moqbus.service.proxy.DbProxy;
import com.moqbus.service.proxy.MqttProxy;

public class ScheduleService {

	static Logger log = Logger.getLogger(ScheduleService.class);
	
	static {
		initAttrMapScheduleListOfSn();
		
		// 注册监听器
		regMonitor2Cache();
	}
	
	private static Long _times = 0L;
	
	
	public static void run() {
		
		Executors.newScheduledThreadPool(10).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

        		log.info("run: times=" + _times);
        		
            	synchronized(_times) {_times++;}
            	
        		// 执行命令
            	execute();
            }
        }, 0, 10, TimeUnit.SECONDS);
        
	}
	
	private  static void execute() {
		
		log.info("_times=" + _times);
		log.info("cacheSchedule.list.size=" + Global.cacheSchedule.list.size());
		
		Map<String, Integer> deviceSnCountMap = new HashMap<String, Integer>();
		
		Global.cacheSchedule.list.forEach((sch)->{
			Integer interval = sch.getInterval();
			String deviceSn = sch.getDeviceSn();
//			log.info("interval=" + interval);
			
			if ((_times-sch.getDelay()) % interval == 0) {
				
				// 首次出现
				Global.threadProxySend.addExecutor(
					()->{
				
//						String deviceSn = sch.getDeviceSn();
						String cmdHex = sch.getCmdHex();
						sendCmd(deviceSn, cmdHex);
						log.info("sendcmd:" + deviceSn + "," + cmdHex);
					}
//					deviceSnCountMap.get(deviceSn)
				);
				
				if (!deviceSnCountMap.containsKey(deviceSn)) {
					deviceSnCountMap.put(deviceSn, 1);
				} else {
					deviceSnCountMap.put(deviceSn, deviceSnCountMap.get(deviceSn)+1);
				}
			}
		});
		
		// 启动延时执行
//		Global.threadProxySend.startDelay();
		//schedule表中配置延时单位,此处不需要延时 2019/8/13
	}
	
	private static void sendCmd(String deviceSn, String cmdHex) {
		
		if (cmdHex == null || StringUtils.isEmpty(cmdHex.trim())) {
			return;
		}
		
		byte[] _cmd = HexHelper.hexStringToBytes(cmdHex); 
		String _topic = JbusConst.TOPIC_PREFIX_CMD + deviceSn;
		MqttProxy.publish(_topic, _cmd);
	}
	
	// deviceSn -> sn,sno entity
	@SuppressWarnings("unchecked")
	private static List<ScheduleEntity> getScheduleListBySn(String deviceSn) {
		
		if (!Global.cacheSchedule.getAttribute("scheduleListOfSn").containsKey(deviceSn)) {
			Global.cacheSchedule.getAttribute("scheduleListOfSn").put(
					deviceSn, new ArrayList<ScheduleEntity>());
		}
		return  (List<ScheduleEntity>)Global.cacheSchedule.getAttribute("scheduleListOfSn").get(deviceSn);
	}
	
	private static void regMonitor2Cache() {

		Global.cacheSchedule.addMonitor(ChangeType.ADD, (deviceSnId, schedule)->{
			log.debug(String.format("schedule added: %s,%s", deviceSnId, schedule.getId()));
			List<ScheduleEntity> schList = getScheduleListBySn(schedule.getDeviceSn());
			schList.add(schedule);
			
		});
		Global.cacheSchedule.addMonitor(ChangeType.UPDATE, (deviceSnId, schedule)->{
			log.debug(String.format("schedule updated: %s,%s", deviceSnId, schedule.getDatPtn()));
			List<ScheduleEntity> schList = getScheduleListBySn(schedule.getDeviceSn());
			ScheduleEntity changedSch = null;
			for(ScheduleEntity s: schList) {
				if (s.getCacheKeyVal().equals(deviceSnId)) {
					changedSch = s;
					break;
				}
			};
			if (changedSch != null) {
				schList.remove(changedSch);
			}
			schList.add(schedule);
		});
		
		Global.cacheSchedule.addMonitor(ChangeType.DELETE, (deviceSnId, schedule)->{
			log.debug(String.format("schedule deleted: %s,%s", deviceSnId, schedule.getId()));
			List<ScheduleEntity> schList = getScheduleListBySn(schedule.getDeviceSn());
			ScheduleEntity changedSch = null;
			for(ScheduleEntity s: schList) {
				if (s.getCacheKeyVal().equals(deviceSnId)) {
					changedSch = s;
					break;
				}
			};
			if (changedSch != null) {
				schList.remove(changedSch);
			}
		});
	}
	private static void initAttrMapScheduleListOfSn() {

		if (Global.cacheSchedule.list.size() == 0) {
			return;
		}

		Map<String, Object> scheduleListOfSnMap = Global.cacheSchedule.getAttribute("scheduleListOfSn");
		scheduleListOfSnMap.clear();
		
		Map<String, List<ScheduleEntity>> map = new HashMap<String, List<ScheduleEntity>>();
		for(ScheduleEntity schedule: Global.cacheSchedule.list) {
			String sn = schedule.getDeviceSn();
			if (!map.containsKey(sn)) {
				map.put(sn, new ArrayList<ScheduleEntity>());
			}
			
			map.get(sn).add(schedule);
			log.info(String.format("initAttrMapScheduleListOfSn: %s, %s", sn, schedule.getDatPtn()));
		}
		
		scheduleListOfSnMap.putAll(map);
	}
	
	@SuppressWarnings("unchecked")
	private static ScheduleEntity getScheduleByData(String deviceSn, byte[] data) {

		List<ScheduleEntity> scheduleList = getScheduleListBySn(deviceSn);
		if (scheduleList == null ||scheduleList.size() == 0) {
			log.info(String.format("no hit by deviceSn not found: %s" , deviceSn));
			return null;
		}

		// 识别返回值
		for(ScheduleEntity sch: scheduleList) {
			String datPtn = sch.getDatPtn();
			
			if (StringUtils.isEmpty(datPtn)) {
				log.info("hit by datPtn==null");
				return sch;
			}
			
			List<Object> ptnList = JsonHelper.json2list(datPtn);
			if (ptnList == null ||  ptnList.size() == 0) {
				continue;
			}
			
			boolean hit = true;
			for(Object ptn: ptnList) {
				int pos = ((List<Double>)ptn).get(0).intValue();
				if (pos >= data.length) {
					hit = false;
					break;
				}

				int val = ((List<Double>)ptn).get(1).intValue();
				int dataVal = ByteHelper.toUnsignedInt(data[pos]);
				if (dataVal != val) {
					hit = false;
					break;
				}
			}
			if (hit) {
				log.info("hit by datPtn=" + datPtn);
				return sch;
			}
		}

		log.info(String.format("no hit : %s, %s" , deviceSn, HexHelper.bytesToHexString(data)));
		return null;
	}
	
	public static void saveDat(String deviceSn, byte[] data, Date time) {
		List<ScheduleEntity> scheduleList = getScheduleListBySn(deviceSn);
		if (scheduleList == null ||scheduleList.size() == 0) {
			return;
		}

		// 识别返回值
		ScheduleEntity schedule = getScheduleByData(deviceSn, data);
		
		if (schedule == null) {
			return;
		}
		
		String deviceSnId = schedule.getCacheKeyVal();
		
		DatDecodeEntity datDecode = Global.cacheDatDecode.map.get(deviceSn);
		
		if (datDecode == null || StringUtils.isEmpty(datDecode.getScriptText())) {
			log.info("datDecode = null :" + deviceSn);
			return;
		}
		
		if (!Global.cacheSchedule.getAttribute("nextTimes").containsKey(deviceSnId)) {
			Global.cacheSchedule.getAttribute("nextTimes").put(deviceSnId, _times);
		}

		Long nextTimes = (Long)Global.cacheSchedule.getAttribute("nextTimes").get(deviceSnId);
		
		if (_times >= nextTimes || schedule.getDataLimit().equals(0)) {

			Global.threadProxyRecv.addExecutor(
				()-> {
	
						String content = parse(deviceSn, data, datDecode.getScriptText());
						if (!StringUtils.isEmpty(content)) {
							DbProxy.saveParsed(JbusConst.TOPIC_PREFIX_DAT, deviceSn, content, data, time);
							Long nextTimesLocal = _times + schedule.getInterval();
							Global.cacheSchedule.getAttribute("nextTimes").put(deviceSnId, nextTimesLocal);
							// 范围check
							checkRange(deviceSn, content, datDecode.getResultSchema());
						} else {
							// 解析失败的，不保存。（msglog保存了所有的cmd/dat）
//							DbProxy.saveOrigin(JbusConst.TOPIC_PREFIX_DAT, deviceSn, data, time);
						}

					}
			);
			
		}
	}

	private static String parse(String deviceSn, byte[] data, String script) {
		
		long startMs  = new Date().getTime();

		try {
			String result = NashornParser.parse(deviceSn, data, script);
			log.info("parse:result=" + result);
			log.info("time elapsed:" + (new Date().getTime()-startMs));
			return result;
			
		} catch (Exception e) {
			log.error(JbusException.trace(e), e);
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static void checkRange(String deviceSn, String content, String schema) {
		Map<String, Object> contentMap = JsonHelper.json2map(content);
		Integer sno = ((Double)contentMap.get("sno")).intValue();
		Map<String, Object> valMap = (Map<String, Object>) contentMap.get("data");
		
		Map<String, Object> schMap = JsonHelper.json2map(schema);
		Map<String, Object> fieldMap = (Map<String, Object>) ((Map<String, Object>) schMap.get(String.valueOf(sno))).get("field");
		
		List<Map<String, Object>> eventDataList = new ArrayList<Map<String, Object>>();
		
		fieldMap.forEach((K,V)->{
			Map<String, String> fieldSch = (Map<String, String>)V;
			float val = Float.valueOf(String.valueOf(valMap.get(K)));
			
			if (fieldSch.containsKey("range")) {
				
				log.info("contains=" + fieldSch.containsKey("range"));
				log.info("range=" + fieldSch.get("range"));
				log.info("K=" + K);
				log.info("val=" + val);
				
				String[] range =  fieldSch.get("range").split(",");
				
				Map<String, Object> eventData = new HashMap<String, Object>();
				eventData.put("deviceSn", deviceSn);
				eventData.put("sno", String.valueOf(sno));
				eventData.put("field", K);
				eventData.put("time", DateHelper.toYmdhms(new Date()));
				
				float min = Float.valueOf(range[0]);
				float max = Float.valueOf(range[1]);
				String fieldName = fieldSch.get("display");

				String lastEvent = EventService._eventHistMap.get(deviceSn + "_" + sno + "_" + K);
				log.info("lastEvent=" + deviceSn + "_" + sno + "_" + K + ": " + lastEvent);
				
				if (val < min) {
					// 阀值报警策略, 同类警报不连续发送
					if (!EventService.EVENT_TYPE_MIN.equals(lastEvent)) {
						
						eventData.put("event", EventService.EVENT_TYPE_MIN);
						eventData.put("detail", String.format("sno:%s, field:%s, act:%s, min:%s", sno,fieldName, val, min));
						eventDataList.add(eventData);
					}
				} else if(val > max) {
					if (!EventService.EVENT_TYPE_MAX.equals(lastEvent)) {
						
						eventData.put("event", EventService.EVENT_TYPE_MAX);
						eventData.put("detail", String.format("sno:%s, field:%s, act:%s, max:%s", sno,fieldName, val, max));
						eventDataList.add(eventData);
					}
				} else {

					if (EventService.EVENT_TYPE_MIN.equals(lastEvent)
							|| EventService.EVENT_TYPE_MAX.equals(lastEvent)) {
						
						if (val < max*(1-0.05) 
								&& val > min*(1+0.05)) {
							
							eventData.put("event", EventService.EVENT_TYPE_NORMAL);
							eventData.put("detail", String.format("sno:%s, field:%s, act:%s, range:%s ~ %s", sno,fieldName, val, min, max));
							eventDataList.add(eventData);
						}
						
					}
				}
			}
			
		});
		
		eventDataList.forEach((E)->{
			EventService.saveEvent(E);
		});
	}
}
