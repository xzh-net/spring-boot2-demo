package net.xzh.ctwing.util;

/**
 * 十六进制字符串工具类
 */
public class HexUtils {

	private HexUtils() {
	}

	/**
	 * 字节数组转十六进制字符串
	 */
	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	/**
	 * 十六进制字符串转字节数组
	 */
	public static byte[] hexToBytes(String hex) {
		int len = hex.length();
		byte[] bytes = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
					+ Character.digit(hex.charAt(i + 1), 16));
		}
		return bytes;
	}
}