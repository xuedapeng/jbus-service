package com.moqbus.service.common.helper;

import java.nio.charset.Charset;

public class ByteHelper {


	public static byte[] str2bytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}
	

	public static int toUnsignedInt(byte b) {
		return  (int) (b & 0xff);

	}

	
	public static int[] toUnsignedInts(byte[] bytes) {

		if (bytes == null) {
			return null;
		}
    	int[] ints = new int[bytes.length];
    	for(int i=0; i<bytes.length; i++) {
    		ints[i] = toUnsignedInt(bytes[i]);
    	}
    	
        return ints;

	}
	
	// 4字节byte转浮点数（IEEE754）
	public static float bit32ToFloat(byte[] b) {
		int bits = byte4ToInt(b);
		float f= Float.intBitsToFloat(bits);
		return f;
	}

    /**
     * 4位字节数组转换为整型
     * @param b
     * @return
     */
    public static int byte4ToInt(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - i));
        }
        return intValue;
    }
}
