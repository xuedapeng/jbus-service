package com.moqbus.service.common.mail;

import com.moqbus.service.common.conf.IConfig;

public interface ICommsProvider {

	public String getProviderInfo();
	public void setConfig(IConfig config);
}
