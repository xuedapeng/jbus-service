package com.moqbus.service.application;

import org.apache.log4j.Logger;

import com.moqbus.service.mqtt.MqttPoolManager;
import com.moqbus.service.proxy.MqttProxy;
import com.moqbus.service.schedule.MailService;
import com.moqbus.service.schedule.ScheduleService;
import com.moqbus.service.application.Global;

public class App {

	static Logger LOG = Logger.getLogger(App.class);
	
	static int IDLE_SECONDS = 60*1000;
	public static void main(String[] args) {
		// 全局配置
		Global.config();
		
		// 初始化mqtt pool
		MqttPoolManager.initialize();
		
		MqttProxy.subscribe();
		
		ScheduleService.run();
		MailService.run();
		
		while(true) {
			try {
				Thread.sleep(IDLE_SECONDS);
			} catch (InterruptedException e) {
				LOG.error("", e);
			}
			
			LOG.info("sleep 60 seconds.");
		}
	}

}
