package com.moqbus.service.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.moqbus.service.application.Global;
import com.moqbus.service.common.mail.ZMailBean;
import com.moqbus.service.common.mail.ZMailManager;

public class MailService {

	static Logger log = Logger.getLogger(MailService.class);
	
	// mailto, mail(合并)
	static Map<String, ZMailBean> _mailtoMap = new HashMap<String, ZMailBean>();

//	static Map<String, List<ZMailBean>> _rangeMap = new HashMap<String, List<ZMailBean>>();
	
	// deviceSn, time
	static Map<String, Date> _lastSendTimeMap = new HashMap<String, Date>();
	static int _SEND_INTERVAL_SENCONDS = 300; // 邮件发送最小间隔秒数
	
	public static void addWarning(String deviceSn, String event, List<ZMailBean> mails) {
		
		// 没有发送记录：立即发送
		if (!_lastSendTimeMap.containsKey(deviceSn)) {
			sendNow(deviceSn, event, mails);
			return;
			
		} else {
			
			Date lastTime = _lastSendTimeMap.get(deviceSn);
			// 有发送记录,且5分钟前：立即发送
			if (isBefore(lastTime, new Date(), -_SEND_INTERVAL_SENCONDS)) {
				sendNow(deviceSn,event, mails);
				return;
			}
		}
		
		// 5分钟内有发送记录，延迟发送
		addMailtoMap(deviceSn, mails);
	}
	
	private static void addMailtoMap(String deviceSn, List<ZMailBean> mails) {
		
		_lastSendTimeMap.put(deviceSn, new Date()); // 抑制立即发送
		
		mails.forEach((M)->{
			String mailtoSn = M.getAddress()+"_"+deviceSn;
    		
			// todo:后覆盖前，（叠加阿里云上发不出去，原因未知）
			if (!_mailtoMap.containsKey(mailtoSn)) {
				M.setContent(M.getContent() + "(YS)");
				_mailtoMap.put(mailtoSn, M);
			} else {
				ZMailBean mail = _mailtoMap.get(mailtoSn);
				mail.setSubject(M.getSubject());
				mail.setContent(
						M.getContent() +
						mail.getContent().substring(mail.getContent().indexOf(("(YS)")))+"*"
						);
				
			}
			log.info(mailtoSn + " => " + _mailtoMap.get(mailtoSn).getContent());
		});
		
		log.info("addMailtoMap:mailtoMap.size=" + _mailtoMap.size());
	}
	
	public static void sendNow(String deviceSn, String event,  List<ZMailBean> mails) {
		_lastSendTimeMap.put(deviceSn, new Date());
		log.info(String.format("立即发送：%s,%s", deviceSn, event));
		ZMailManager.send(mails);
	}

	public static void run() {
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
	        @Override
	        public void run() {
	        	
	    		// 执行命令
	        	execute();
	        }
	    }, 0, _SEND_INTERVAL_SENCONDS, TimeUnit.SECONDS); // 5分钟内只发送一条通知。
	}
	
	protected static void execute() {
		
		if (_mailtoMap.size() == 0) {
			return;
		}

		Map<String, ZMailBean> tmpMap = _mailtoMap;
    	_mailtoMap = new HashMap<String, ZMailBean>();

    	tmpMap.forEach((K,V)->{
    		String deviceSn = K.split("_")[1];
    		_lastSendTimeMap.put(deviceSn, new Date());
    	});
    	
    	Global.executor4SendMail.execute(()->{
    		
    		// 半数平均，10秒，取较大的值
    		int sleep = Math.max(10000,(_SEND_INTERVAL_SENCONDS/2/tmpMap.size())*1000);
    		
	    	tmpMap.forEach((K,V)->{
	    		String deviceSn = K.split("_")[1];
	    		_lastSendTimeMap.put(deviceSn, new Date());
	    		log.info(String.format("延时发送：%s,%s", deviceSn, V.getSubject()));
		    	ZMailManager.send(V);
		    	try {
					Thread.sleep(sleep); // 发个邮件延时10秒，防止受限
				} catch (InterruptedException e) {
					log.error("", e);
				}
	    	});
    	});
	}
	
	private static boolean isBefore(Date t1, Date t2, int sec) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(t2);
		cal.add(Calendar.SECOND, sec);
		
		return t1.before(cal.getTime());
	}
}
