package jsapi;

import com.moqbus.service.common.helper.ByteHelper;

public class Math {

	// 4字节byte转浮点数（IEEE754）
	public static float bit32ToFloat(byte[] b) {
		
		return ByteHelper.bit32ToFloat(b);
	}
}
