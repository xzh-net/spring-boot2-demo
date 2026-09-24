package net.xzh.ctwing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.xzh.ctwing.util.HexUtils;

/**
 * 十六进制工具单元测试
 */
class HexUtilsTest {

	@Test
	void bytesAndHexRoundTrip() {
		byte[] bytes = new byte[] { 0x01, 0x05, 0x00, 0x00, (byte) 0xFF, 0x00 };
		String hex = HexUtils.bytesToHex(bytes);
		assertEquals("01050000FF00", hex);
		assertArrayEquals(bytes, HexUtils.hexToBytes(hex));
	}
}