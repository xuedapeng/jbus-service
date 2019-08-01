package com.moqbus.service.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.moqbus.service.application.Global;
import com.moqbus.service.common.conf.ZSystemConfig;
import com.moqbus.service.common.helper.DateHelper;
import com.moqbus.service.common.helper.JsonHelper;
import com.moqbus.service.common.mail.ZMailBean;
import com.moqbus.service.common.mail.ZMailManager;
import com.moqbus.service.db.mysql.bean.EventEntity;
import com.moqbus.service.db.mysql.bean.WarningContactEntity;
import com.moqbus.service.db.mysql.dao.EventDao;


// 设备上下线信息相关服务
public class EventService {

	static Logger log = Logger.getLogger(EventService.class);
	
	static final String EVENT_TYPE_ON = "on";
	static final String EVENT_TYPE_OFF = "off";
	static final String EVENT_TYPE_MAX = "max";
	static final String EVENT_TYPE_MIN = "min";
	static final String EVENT_TYPE_NORMAL = "normal";
	
	
	static Map<String, Integer> _eventMaskMap = ImmutableMap.of(
			"on", 1, 
			"off", 2, 
			"max", 4, 
			"min", 8,
			"normal", 16
			);
	
	static Map<String, String> _eventNameMap = ImmutableMap.of(
			"on", "设备上线", 
			"off", "设备下线", 
			"max", "大于阀值上限", 
			"min", "小于阀值下限",
			"normal", "回归阀值范围"
			);
	
	static Map<String, String> _eventHistMap = new HashMap<String, String>();
	static {
//		clearHistoryTask();
	}
	
	// data: deviceSn, sno, field, time, event, detail
	public static void saveEvent(Map<String, Object> data) {

		String deviceSn = (String)data.get("deviceSn");
		String sno = (String) JsonHelper.ifnull(data.get("sno"), "");
		String field = (String) JsonHelper.ifnull(data.get("field"), "");
		String time = (String)data.get("time");
		String event = (String)data.get("event");
		String detail = (String)data.get("detail");
		String memo = (String) JsonHelper.ifnull(detail, JsonHelper.map2json(data));
		
		EventEntity eventEntity = new EventEntity();
		eventEntity.setDeviceId(Global.cacheDevice.map.get(deviceSn).getId());
		eventEntity.setDeviceSn(deviceSn);
		eventEntity.setEvent(event);
		eventEntity.setTime(DateHelper.fromYmdhms(time));
		eventEntity.setMemo(memo);
		
		if (deviceSn == null || deviceSn.length() > 10) {
			return;
		}
		
		Global.threadProxyEvent.addExecutor(()->{

			EventDao.insert(eventEntity);
			
			log.info("save event: " + memo);
		});

		// 阀值报警策略, 同类警报不连续发送
		_eventHistMap.put(deviceSn + "_" + sno + "_" + field, event);
		log.info("event=" + deviceSn + "_" + sno + "_" + field + ":" + event);
		
		sendWarning(eventEntity);
	}
	
	public static void sendWarning(EventEntity eventEntity) {

		WarningContactEntity contact = Global.cacheWarningContact.map.get(eventEntity.getDeviceSn());
		if (contact == null) {
			return;
		}
		
		if (!_eventMaskMap.containsKey(eventEntity.getEvent())) {
			return;
		}
		
		int eventMask = _eventMaskMap.get(eventEntity.getEvent());
		int recvEvent = contact.getEvent();
		
		if ((recvEvent&eventMask) != eventMask) {
			return;
		}
		
		Global.threadProxyWarning.addExecutor(()->{
			
			String mail = contact.getEmail();
			String[] to = mail.split(",");
			String bcc = mail.substring(mail.indexOf(",")+1);
			List<ZMailBean> mailList = new ArrayList<ZMailBean>();
			// 发送邮件通知
			for(String t : to) {
				ZMailBean mailBean = new ZMailBean();
				mailBean.setAddress(t);
//					mailBean.setBcc(bcc);
				mailBean.setSubject(String.format("%s通知(%s)",
						_eventNameMap.get(eventEntity.getEvent()),
						Global.cacheDevice.map.get(eventEntity.getDeviceSn()).getDeviceName()));
				mailBean.setContent(String.format("设备编号：%s <br/> 事件类型：%s <br/> 时间：%s  <br/><br/> ---------------<br/> %s ", 
						eventEntity.getDeviceSn(),
						_eventNameMap.get(eventEntity.getEvent()),
						DateHelper.toYmdhms(eventEntity.getTime()),
						eventEntity.getMemo()));
				mailBean.setFromNickname("moqbus service");
				mailBean.setFromAddressDisp(ZSystemConfig.getProperty("mail_send_account"));
				mailList.add(mailBean);
			}
//			ZMailManager.send(mailList);
			MailService.addWarning(eventEntity.getDeviceSn(), eventEntity.getEvent(), mailList);
			log.info("addWarning: " + eventEntity.getMemo());
				
			
		});
	}
	
	// 每天8时清空事件历史，同类警报重复发送一次
//	private static void clearHistoryTask() {
//		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
//	        @Override
//	        public void run() {
//	        	Calendar now = Calendar.getInstance();
//	        	if (now.get(Calendar.HOUR_OF_DAY) == 8 && now.get(Calendar.MINUTE) == 0) {
//		        	_eventHistMap.clear();
//	        	}
//	        }
//	    }, 0, 60, TimeUnit.SECONDS);
//	}
}


