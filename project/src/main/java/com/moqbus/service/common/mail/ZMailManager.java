package com.moqbus.service.common.mail;

import java.util.List;

import org.apache.log4j.Logger;

import com.moqbus.service.common.conf.IConfig;
import com.moqbus.service.common.conf.ZSystemConfig;


public class ZMailManager {

	static Logger logger = Logger.getLogger(ZMailManager.class);
	
	private static final String PROP_PROVIDER_CLASS = "mail_provider_class";
	private static IMailProvider mProvider = null;
	private static IConfig mConfig = ZSystemConfig.getInstance();
	
	public static void setConfig(IConfig config) {
		mConfig = config;
	}
	
	// 设置提供商的实现类
	public static void setProvider(IMailProvider provider) {
		mProvider = provider;
	}
	
	private static IMailProvider getProvider()  {
		if (mProvider == null) {
			String className = mConfig.getProp(PROP_PROVIDER_CLASS);
			try {
				mProvider = (IMailProvider)Class.forName(className).newInstance();
				mProvider.setConfig(mConfig);
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				
				logger.error("", e);
			}
		}
		
		return mProvider;
	}

	// 批量、同步
	public static List<ZMailResult> send(List<ZMailBean> mailBeanList) {
		
		List<ZMailResult> resultList = getProvider().send(mailBeanList);
		return resultList;
	}

	// 单个、同步
	public static ZMailResult send(ZMailBean mailBean) {
		
		ZMailResult result = getProvider().send(mailBean);
		
		return result;
	}
	
	// 批量、异步
	public static void sendAsync(final List<ZMailBean> mailBeanList) {

		final IMailProvider thisProvider = getProvider();
		
		new Thread(new Runnable() {
			public void run() {
				thisProvider.send(mailBeanList);
			}
		}).start();
		
	}
	
	// 单个、异步
	public static void sendAsync(final ZMailBean mailBean) {

		final IMailProvider thisProvider = getProvider();
		
		new Thread(new Runnable() {
			public void run() {
				thisProvider.send(mailBean);
			}
		}).start();
		
	}
	
}
