package com.moqbus.service.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.moqbus.service.common.exception.JbusException;


public class ThreadProxy {

	static Logger log = Logger.getLogger(ThreadProxy.class);
	
	
//	final static int MAX_THREADS = 10;
//	final static int DEFAULT_THREADS = 5;
//	final static int DEFAULT_DELAY = 1000; 
//	final static int MIN_DELAY = 10; 
	
	Executor _executor = null;
	
	// 延时执行, *100ms, 最多25,500ms
	Map<Integer, List<Runnable>> _delayRunnableListMap = new HashMap<Integer, List<Runnable>>();
//	List<Executor2> _executorList = new ArrayList<Executor2>();
//	List<MyThread> _threadList = new ArrayList<MyThread>();
//	int _threadAmount;
//	int _delay;
//	String _threadId;

	public ThreadProxy(int threadAmount, int delay, String threadId) {
		
		_executor = Executors.newFixedThreadPool(threadAmount);
		
//		_threadAmount = threadAmount > MAX_THREADS?MAX_THREADS:(threadAmount<1?1:threadAmount);
//		
//		_delay = delay < MIN_DELAY ?MIN_DELAY:delay;
//		_threadId = threadId;
//		init();
//		
//		log.info(String.format("ThreadProxy(threadAmount=%d, delay=%d, threadId=%s)", 
//				threadAmount, delay, threadId));
	}
	
//	public interface Executor2 {
//		public  void run();
//	}
	

	public void addExecutor(Runnable runnable, Integer delay) {
		
		if (delay == null || delay.equals(0)) {
			addExecutor(runnable);
			return;
		}

//		log.info("delay:"+ delay);
		List<Runnable> list = _delayRunnableListMap.get(delay);
		if (list == null) {
			list = new ArrayList<Runnable>();
			_delayRunnableListMap.put(delay, list);
		}
		
		list.add(runnable);
		
		
	}
	
	public void startDelay() {
		
		log.info("_delayRunnableListMap.size:"+ _delayRunnableListMap.size());
		if (_delayRunnableListMap.isEmpty()) {
			return;
		}
		
		Executors.newFixedThreadPool(1).execute(()->{
			for(int i = 1; i<256; i++) {

				log.info("_delayRunnableListMap.size2:"+ _delayRunnableListMap.size());
				log.info("i:"+ i);
				if (_delayRunnableListMap.isEmpty()) {
					break;
				}
				
				try {
					Thread.sleep(100); // 每个传感器延时100毫秒
					
					log.info("_delayRunnableListMap.containsKey(i):"+ _delayRunnableListMap.containsKey(i));
					if (_delayRunnableListMap.containsKey(i)) {
						_delayRunnableListMap.get(i).forEach(R->{
							_executor.execute(R);
						});
						_delayRunnableListMap.remove(i);
					}
				} catch (InterruptedException e) {
					log.error(JbusException.trace(e), e);
				}
			}
			
			_delayRunnableListMap.clear();
		});
	}
	
	public void addExecutor(Runnable runnable) {
		_executor.execute(runnable);
	}
	
//	private void init() {
//		
//		for (int i=0; i<_threadAmount; i++) {
//
//			log.info(String.format("init:i=%d", i));
//			MyThread mt = new MyThread(_threadId + "_" + i);
//			_threadList.add(mt);
//			mt.run();
//		}
//		
//		run();
//	}
//	
//	private void run() {
//		new Thread() {
//
//			@Override
//			public void run() {
//
//				int nextIdx = 0;
//				while(true) {
//					
//					if(!_executorList.isEmpty()) {
//
//						List<Executor> runList = _executorList;
//						_executorList = new ArrayList<Executor>();
//						
//						_threadList.get(nextIdx).addExecutor(runList);
//						nextIdx++;
//						if (nextIdx == _threadAmount) {
//							nextIdx = 0;
//						}
//					}
//					
//					try {
//						Thread.sleep(_delay);
//					} catch (InterruptedException e) {
//						log.error("", e);
//					}
//				}
//				
//			}
//			
//		}.start();
//	}
//	
//	private class MyThread implements Runnable {
//		
//		private List<Executor> _executorList = new ArrayList<Executor>();
//		private String _threadId;
//		
//		public  void addExecutor(List<Executor> executorList) {
//			_executorList.addAll(executorList);
//		}
//		
//		public MyThread(String threadId) {
//			_threadId = threadId;
//		}
//		
//		@Override
//		public void run() {
//			
//			new Thread() {
//				@Override  
//				public void run() {
//
//					while (true) {
//
//						if (!_executorList.isEmpty()) {
//
//							log.info(String.format("threadId=%s, _executorList.size=%d", _threadId,  _executorList.size()));
//							
//							List<Executor> runList = _executorList;
//							_executorList = new ArrayList<Executor>();
//							
//							runList.forEach((E)->{
//								E.run();
//							});
//						}
//
//							try {
//								Thread.sleep(_delay);
//							} catch (InterruptedException e) {
//								log.error("", e);
//							}
//					}
//				}
//			}.start();
//		}
//	}
}
