package com.moqbus.service.codec;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.Invocable;

import org.apache.log4j.Logger;

import com.moqbus.service.common.helper.ByteHelper;
import com.moqbus.service.common.helper.HexHelper;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;

public class NashornParser {

	static Logger log = Logger.getLogger(NashornParser.class);
	
	final static int MAX_CAPACITY = 10;
	static BlockingQueue<NashornSandbox> _queue = new LinkedBlockingQueue<NashornSandbox>(MAX_CAPACITY);
	static {
		for (int i=0; i<MAX_CAPACITY; i++) {
			_queue.offer(create());
		}
	}

	
	public static NashornSandbox create() {
		
		NashornSandbox _nashornSandbox = NashornSandboxes.create();
		
		_nashornSandbox.setMaxCPUTime(100);
		_nashornSandbox.setMaxMemory(10*1024*1024);
		_nashornSandbox.allowNoBraces(false);
		_nashornSandbox.setMaxPreparedStatements(30); // because preparing scripts for execution is expensive
		_nashornSandbox.setExecutor(Executors.newSingleThreadExecutor());
		
		return _nashornSandbox;
	}
	
	
	public static String parse(String deviceSn, byte[] data, String script) {
		
		String dataStr = HexHelper.bytesToHexString(data);
		log.info(String.format("NashornParser#parser:%s, %s", deviceSn, dataStr));
		
		String result = ParseCache.get(deviceSn, dataStr);
		if ( result != null) {
			log.info("NashornParser cache hit.");
			return result;
		}
		
		NashornSandbox ns = null;
		try {
			ns = _queue.take();
			ns.eval(script);
			Invocable invocable = ns.getSandboxedInvocable();
			result = (String)invocable.invokeFunction("decodeDat", ByteHelper.toUnsignedInts(data));
			ParseCache.put(deviceSn, dataStr, result);
			
			return result;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (ns != null) {
				_queue.offer(ns);
			}
		}
		
		return null;
	}
	
}
