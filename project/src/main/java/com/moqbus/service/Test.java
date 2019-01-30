package com.moqbus.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptException;

import com.moqbus.service.codec.NashornParser;
import com.moqbus.service.db.mysql.bean.DatDecodeEntity;
import com.moqbus.service.db.mysql.bean.EventEntity;
import com.moqbus.service.db.mysql.cache.CacheableEntity;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException;

public class Test {
	public static void main(String[] args) throws ScriptCPUAbuseException, ScriptException, NoSuchMethodException {
			
		Object o = null;
		String s = (String)o;
		System.out.println(s);
	}
	
	
}



