package com.moqbus.service.common.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;


public class DateHelper {

	static Logger log = Logger.getLogger(DateHelper.class);
	
	static SimpleDateFormat _ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String toYmdhms(Date date) {
		return _ymdhms.format(date);
	}
	
	public static Date fromYmdhms(String dateStr) {
		try {
			return _ymdhms.parse(dateStr);
		} catch (ParseException e) {
			
			log.error(e.getMessage(), e);
			
			return null;
		}
	}
}
