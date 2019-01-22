package com.moqbus.service.proxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class ThreadProxy {

	static Logger log = Logger.getLogger(ThreadProxy.class);
	
	final static int MAX_THREADS = 10;
	final static int DEFAULT_THREADS = 5;
	final static int DEFAULT_DELAY = 1000; 
	final static int MIN_DELAY = 10; 
	

	List<Executor> _executorList = new ArrayList<Executor>();
	List<MyThread> _threadList = new ArrayList<MyThread>();
	int _threadAmount;
	int _delay;
	String _threadId;

	public ThreadProxy(int threadAmount, int delay, String threadId) {
		
		_threadAmount = threadAmount > MAX_THREADS?MAX_THREADS:(threadAmount<1?1:threadAmount);
		
		_delay = delay < MIN_DELAY ?MIN_DELAY:delay;
		_threadId = threadId;
		init();
		
		log.info(String.format("ThreadProxy(threadAmount=%d, delay=%d, threadId=%s)", 
				threadAmount, delay, threadId));
	}
	
	public interface Executor {
		public  void run();
	}
	
	
	public void addExecutor(Executor executor) {
		_executorList.add(executor);
	}
	
	private void init() {
		
		for (int i=0; i<_threadAmount; i++) {

			log.info(String.format("init:i=%d", i));
			MyThread mt = new MyThread(_threadId + "_" + i);
			_threadList.add(mt);
			mt.run();
		}
		
		run();
	}
	
	private void run() {
		new Thread() {

			@Override
			public void run() {

				int nextIdx = 0;
				while(true) {
					
					if(!_executorList.isEmpty()) {

						List<Executor> runList = _executorList;
						_executorList = new ArrayList<Executor>();
						
						_threadList.get(nextIdx).addExecutor(runList);
						nextIdx++;
						if (nextIdx == _threadAmount) {
							nextIdx = 0;
						}
					}
					
					try {
						Thread.sleep(_delay);
					} catch (InterruptedException e) {
						log.error("", e);
					}
				}
				
			}
			
		}.start();
	}
	
	private class MyThread implements Runnable {
		
		private List<Executor> _executorList = new ArrayList<Executor>();
		private String _threadId;
		
		public  void addExecutor(List<Executor> executorList) {
			_executorList.addAll(executorList);
		}
		
		public MyThread(String threadId) {
			_threadId = threadId;
		}
		
		@Override
		public void run() {
			
			new Thread() {
				@Override  
				public void run() {

					while (true) {

						if (!_executorList.isEmpty()) {

							log.info(String.format("threadId=%s, _executorList.size=%d", _threadId,  _executorList.size()));
							
							List<Executor> runList = _executorList;
							_executorList = new ArrayList<Executor>();
							
							runList.forEach((E)->{
								E.run();
							});
						}

							try {
								Thread.sleep(_delay);
							} catch (InterruptedException e) {
								log.error("", e);
							}
					}
				}
			}.start();
		}
	}
}
