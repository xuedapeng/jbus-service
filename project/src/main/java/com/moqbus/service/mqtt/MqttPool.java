package com.moqbus.service.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.moqbus.service.common.exception.JbusException;
import com.moqbus.service.common.helper.CryptoHelper;
import com.moqbus.service.proxy.MqttProxy;

public class MqttPool {

	static Logger logger = Logger.getLogger(MqttPool.class);
	
	// MqttClient
	 List<MqttClient> _mqttClientList = new ArrayList<MqttClient>();
	
	// connection lost
	 List<MqttClient> _mqttClientDisconnectedList = new ArrayList<MqttClient>();
	
	 private String mqttBroker;
	 private String mqttUsername;
	 private String mqttPassword;
	
	 int INIT_SIZE = 1;
	 final long IDLE_SLEEP = 10*1000;// 休眠10秒；
	
	 MqttConnectOptions _connOpts = new MqttConnectOptions(); 
	
	// 初始创建10个连接
	public  void initialize(String broker, String username, String password) {
		mqttBroker = broker;
		mqttUsername = username;
		mqttPassword = password;
		
		initPool();
		checkLostTask();
	}
	
	private  void initPool() {

		_connOpts.setCleanSession(true);  
		_connOpts.setUserName(mqttUsername);  
		_connOpts.setPassword(mqttPassword.toCharArray());  
		_connOpts.setConnectionTimeout(10);  
		_connOpts.setKeepAliveInterval(20); 
        
		try {
			for (int i=0; i<INIT_SIZE; i++ ) {
					MqttClient client = createMqttClient();
					_mqttClientList.add(client);
					
					logger.info("MqttClient created. clientId=" + client.getClientId());
			}
			
		} catch (MqttException e) {
			
			logger.error("无法连接到mqtt服务器。", e);
			throw new JbusException(e);
		}
	}
	
	// 重连线程
	private  void checkLostTask() {
		
		new Thread() {

			@Override  
			public void run() {
				
				while(true) {
					
					if (_mqttClientDisconnectedList.size() > 0) {
						reconnect();
						logger.info("_mqttClientDisconnectedList.size=" + _mqttClientDisconnectedList.size());
					}

					
					try {
						sleep(IDLE_SLEEP);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
				}
			}
			
		}.start();
	}
	
	private  synchronized void reconnect() {

		List<MqttClient> successList = new ArrayList<MqttClient>();
		
		// 重连接
		_mqttClientDisconnectedList.forEach((E)->{
			try {
				if (!E.isConnected()) {
					E.connect(_connOpts);
					// 重新订阅
					MqttProxy.doAfterReconnect();
					logger.info("reconnected. client=" + E.getClientId());
				}
				successList.add(E);
			} catch (MqttException e) {
				logger.error("", e);
			}
		});
		
		// 从失连列表中删除重连成功的对象
		successList.forEach((E)->{
			_mqttClientDisconnectedList.remove(E);
		});
		
		// 全面检查
		_mqttClientList.forEach((E)->{

			if (!E.isConnected()) {
				if (!_mqttClientDisconnectedList.contains(E)) {
					_mqttClientDisconnectedList.add(E);
				}
			}
		});

		
	}

	public  MqttClient getInstance() {
		return whoNotBusy();
	}
	
	private  MqttClient createMqttClient() throws MqttException {
		
        MemoryPersistence persistence = new MemoryPersistence();  
        
        
        MqttClient mqttClient = new MqttClient(mqttBroker, makeClientId(), persistence);  
        mqttClient.setCallback(new MqttCallback(){

			@Override
			public void connectionLost(Throwable cause) {
				
				logger.info("", cause);
				// 重连
				if (!_mqttClientDisconnectedList.contains(mqttClient)) {
					_mqttClientDisconnectedList.add(mqttClient);
				}
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				
				
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				
				MqttProxy.recieve(topic, message);
				
			}
        	
        });  
        
        
        mqttClient.connect(_connOpts);  
        return mqttClient;  
	}

	private  MqttClient whoNotBusy() {
		
		return _mqttClientList.get(0);
		
	}
	
	private  String makeClientId() {
		return CryptoHelper.genUUID();
	}

}
