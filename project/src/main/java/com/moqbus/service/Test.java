package com.moqbus.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptException;

import com.moqbus.service.application.Global;
import com.moqbus.service.codec.NashornParser;
import com.moqbus.service.common.conf.ZSystemConfig;
import com.moqbus.service.common.helper.DateHelper;
import com.moqbus.service.common.mail.ZMailBean;
import com.moqbus.service.common.mail.ZMailManager;
import com.moqbus.service.db.mysql.bean.DatDecodeEntity;
import com.moqbus.service.db.mysql.bean.EventEntity;
import com.moqbus.service.db.mysql.cache.CacheableEntity;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException;

public class Test {
	public static void main(String[] args) throws ScriptCPUAbuseException, ScriptException, NoSuchMethodException {
			

		String c = "设备编号：7E35CBF4 <br/> 事件类型：设备下线 <br/> 时间：2019-07-29 13:21:08  <br/><br/> ---------------<br/> {\"onlineCount\":\"0\",\"tcpClient\":\"47.92.87.25:38450\",\"time\":\"2019-07-29 13:21:08\",\"sessionId\":\"1809de7caa9547c8b0215e47a1f2214f\",\"event\":\"off\",\"deviceSn\":\"7E35CBF4\"}(YS)*";
		
		String s = c.substring(c.indexOf("(YS)"));
		
		System.out.println(s);
		
	}
	

}



