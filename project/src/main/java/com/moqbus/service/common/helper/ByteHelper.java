package com.moqbus.service.common.helper;

import java.nio.charset.Charset;

public class ByteHelper {


	public static byte[] str2bytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}
}
