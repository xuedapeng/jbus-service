package com.moqbus.service.schedule;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.script.Invocable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.moqbus.service.application.Global;
import com.moqbus.service.common.constant.JbusConst;
import com.moqbus.service.common.helper.HexHelper;
import com.moqbus.service.db.mysql.bean.DatDecodeEntity;
import com.moqbus.service.db.mysql.bean.ScheduleEntity;
import com.moqbus.service.db.mysql.cache.TableCache;
import com.moqbus.service.db.mysql.dao.DatDecodeDao;
import com.moqbus.service.db.mysql.dao.ScheduleDao;
import com.moqbus.service.proxy.DbProxy;
import com.moqbus.service.proxy.MqttProxy;
import com.moqbus.service.proxy.ThreadProxy;
import com.moqbus.service.proxy.ThreadProxy.Executor;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;

public class ScheduleService {

	static Logger log = Logger.getLogger(ScheduleService.class);
	
//	static List<VScheduleEntity> _scheduleList = new ArrayList<VScheduleEntity>();
//	static Map<String, VScheduleEntity> _deviceSn2Entity = new HashMap<String, VScheduleEntity>();
//	static Map<String, Long> _deviceSn2NextTimes = new HashMap<String, Long>();

//	static ScriptEngine _nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
	static NashornSandbox _nashornSandbox = NashornSandboxes.create();
	static {
		_nashornSandbox.setMaxCPUTime(100);
		_nashornSandbox.setMaxMemory(10*1024*1024);
		_nashornSandbox.allowNoBraces(false);
		_nashornSandbox.setMaxPreparedStatements(30); // because preparing scripts for execution is expensive
		_nashornSandbox.setExecutor(Executors.newSingleThreadExecutor());
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
		
		Global.cacheSchedule.list.forEach((sch)->{
			Integer interval = sch.getInterval();

			log.info("interval=" + interval);
			
			if (_times % interval == 0) {
				Global.threadProxySend.addExecutor(
					new Executor() {
						@Override
						public void run() {
							String deviceSn = sch.getDeviceSn();
							String cmdHex = sch.getCmdHex();
							sendCmd(deviceSn, cmdHex);
							log.info("sendcmd:" + deviceSn + "," + cmdHex);
						}
					}
				);
			}
		});
	}
	
	private static void sendCmd(String deviceSn, String cmdHex) {
		byte[] _cmd = HexHelper.hexStringToBytes(cmdHex); 
		String _topic = JbusConst.TOPIC_PREFIX_CMD + deviceSn;
		MqttProxy.publish(_topic, _cmd);
	}
	
	public static void saveDat(String deviceSn, byte[] data, Date time) {
		if (!Global.cacheSchedule.map.containsKey(deviceSn)) {
			return;
		}
		
		DatDecodeEntity datDecode = Global.cacheDatDecode.map.get(deviceSn);
		ScheduleEntity schedule = Global.cacheSchedule.map.get(deviceSn);
		
		if (datDecode == null || StringUtils.isEmpty(datDecode.getScriptText())) {
			log.info("datDecode = null :" + deviceSn);
			return;
		}
		
		if (!Global.cacheSchedule.getAttribute("nextTimes").containsKey(deviceSn)) {
			Global.cacheSchedule.getAttribute("nextTimes").put(deviceSn, _times);
		}

		Long nextTimes = (Long)Global.cacheSchedule.getAttribute("nextTimes").get(deviceSn);
		
		if (_times >= nextTimes) {

			Global.threadProxyRecv.addExecutor(
				new Executor() {
					@Override
					public void run() {
	
						String content = parse(deviceSn, data, datDecode);
						if (!StringUtils.isEmpty(content)) {
							DbProxy.saveParsed(JbusConst.TOPIC_PREFIX_DAT, deviceSn, content, data, time);
							Long nextTimes = _times + schedule.getInterval();
							Global.cacheSchedule.getAttribute("nextTimes").put(deviceSn, nextTimes);
						} else {
							DbProxy.saveOrigin(JbusConst.TOPIC_PREFIX_DAT, deviceSn, data, time);
						}

					}
				}
			);
			
		}
	}

	private static String parse(String deviceSn, byte[] data, DatDecodeEntity datDecode) {
		
		long startMs  = new Date().getTime();
		try {
			_nashornSandbox.eval(datDecode.getScriptText());
			Invocable invocable = _nashornSandbox.getSandboxedInvocable();

			String result = (String)invocable.invokeFunction("decodeDat", data);
			
			log.info("parse:result=" + result);
			log.info("time elapsed:" + (new Date().getTime()-startMs));
			
			return result;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}
}
