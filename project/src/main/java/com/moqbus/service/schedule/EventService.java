package com.moqbus.service.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.moqbus.service.proxy.ThreadProxy.Executor;


// 设备上下线信息相关服务
public class EventService {

	static Logger log = Logger.getLogger(EventService.class);
	
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
	
	public static void saveEvent(Map<String, Object> data) {

		String deviceSn = (String)data.get("deviceSn");
		String time = (String)data.get("time");
		String event = (String)data.get("event");
		String memo = JsonHelper.map2json(data);
		
		EventEntity eventEntity = new EventEntity();
		eventEntity.setDeviceSn(deviceSn);
		eventEntity.setEvent(event);
		eventEntity.setTime(DateHelper.fromYmdhms(time));
		eventEntity.setMemo(memo);
		
		if (deviceSn == null || deviceSn.length() > 10) {
			return;
		}
		
		Global.threadProxyEvent.addExecutor(new Executor() {

			@Override
			public void run() {

				EventDao.insert(eventEntity);
				
				log.info("save event: " + memo);
				
			}
			
		});

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
		
		// todo:阀值报警策略, 
//		String lastEvent = _eventHistMap.get(eventEntity.getDeviceSn());
//		
//		if (eventEntity.getEvent().equals(lastEvent)) {
//			return;
//		} else {
//			_eventHistMap.put(eventEntity.getDeviceSn(), eventEntity.getEvent());
//		}
		
		Global.threadProxyWarning.addExecutor(new Executor() {

			@Override
			public void run() {

				String mail = contact.getEmail();
				String[] to = mail.split(",");
				String bcc = mail.substring(mail.indexOf(",")+1);
				List<ZMailBean> mailList = new ArrayList<ZMailBean>();
				// 发送邮件通知
				for(String t : to) {
					ZMailBean mailBean = new ZMailBean();
					mailBean.setAddress(t);
//					mailBean.setBcc(bcc);
					mailBean.setSubject(String.format("通知:%s(%s)",
							_eventNameMap.get(eventEntity.getEvent()),
							eventEntity.getDeviceSn()));
					mailBean.setContent(String.format("发生报警事件，请及时处理。   \n ---------------\n%s ", 
							eventEntity.getMemo()));
					mailBean.setFromNickname("moqbus service");
					mailBean.setFromAddressDisp(ZSystemConfig.getProperty("mail_send_account"));
					mailList.add(mailBean);
				}
				ZMailManager.send(mailList);
				log.info("send mail: " + eventEntity.getMemo());
				
			}
			
		});
	}

}


