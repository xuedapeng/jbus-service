package com.moqbus.service.proxy;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.moqbus.service.application.Global;
import com.moqbus.service.common.constant.JbusConst;
import com.moqbus.service.common.exception.JbusException;
import com.moqbus.service.common.helper.HexHelper;
import com.moqbus.service.common.helper.JsonHelper;
import com.moqbus.service.mqtt.MqttPoolManager;
import com.moqbus.service.schedule.EventService;
import com.moqbus.service.schedule.ScheduleService;


public class MqttProxy {

	static Logger log = Logger.getLogger(MqttProxy.class);

	public static void publish(String topic, byte[] data) {
		try {

			log.info(
					String.format("before:publish:topic=%s, data=[%s]", 
							topic, 
							HexHelper.bytesToHexString(data)));
			
			MqttMessage mm = new MqttMessage(data);
			mm.setQos(0);
			MqttPoolManager.getMqttPool().getInstance().publish(topic, mm);
			
			log.info(
					String.format("after:publish:topic=%s, data=[%s]", 
							topic, 
							HexHelper.bytesToHexString(data)));
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void subscribe() {

		log.info(String.format("subscribe"));
		
		try {
//			MqttPoolManager.getMqttPool().getInstance().subscribe(JbusConst.TOPIC_PREFIX_CMD + "+");
			MqttPoolManager.getMqttPool().getInstance().subscribe(JbusConst.TOPIC_PREFIX_DAT + "+");
			MqttPoolManager.getMqttPool().getInstance().subscribe(JbusConst.TOPIC_PREFIX_STS + "+");
			
		} catch (MqttException e) {
			log.error("订阅失败", e);
			throw new JbusException(e);
		}
	}
	
	public static void doAfterReconnect() {
		subscribe();
	}
	
	// mqtt client 收到推送时调用此方法
	public static void recieve(String topic, MqttMessage message) {

		String deviceSn = getDeviceId(topic);
		String topicType = getTopicType(topic);
		byte[] payload = message.getPayload();

		// 未知设备
		if (!Global.cacheDevice.map.containsKey(deviceSn)) {
			return;
		}
		
		String content = "";
		if (topicType.equals(JbusConst.TOPIC_PREFIX_DAT)) {
			
			content = HexHelper.bytesToHexString(payload);
			ScheduleService.saveDat(deviceSn, payload, new Date());
			
		} else if(topicType.equals(JbusConst.TOPIC_PREFIX_STS)) {
			try {
				content = new String(payload, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("", e);
				return;
			}
			EventService.saveEvent(JsonHelper.json2map(content));
		}

		log.info(String.format("recieve: %s->%s", 
				topic, content));
		 
	}
	
	// return deviceId
	private static String getDeviceId(String topic) {
		return topic.substring(topic.lastIndexOf("/")+1);
	}
	
	private static String getTopicType(String topic) {
		return topic.substring(0, topic.lastIndexOf("/")+1);
	}
	
}
