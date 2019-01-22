package com.moqbus.service.common.mail;

import java.util.List;


public interface IMailProvider extends ICommsProvider{
	
	public List<ZMailResult> send(List<ZMailBean> mailBeanList);
	public ZMailResult send(ZMailBean mailBean);

}
