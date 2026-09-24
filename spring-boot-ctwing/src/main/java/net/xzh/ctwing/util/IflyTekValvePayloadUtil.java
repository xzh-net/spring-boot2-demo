package net.xzh.ctwing.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * 讯飞（慧种田 App 同款）电动球阀下行报文封装
 *
 * <p>报文格式（与慧种田 {@code IflyTekPayloadWrapper} 一致）：<br>
 * 功能码(1B=0x81) | 设备SN(15B) | 包序号(1B=0x01) | 包标记(1B=0x01) |
 * 数据长度(2B,大端) | 数据域(JSON,nB) | 校验码(2B,大端)</p>
 *
 * <p>数据域 JSON：<br>
 * 单阀 {@code {"para_set":{"id":taskNo,"data":{"dir":0-3,"open":0-100,...}}}}<br>
 * 多头阀 {@code {"para_set":{"id":taskNo,"data":{"openlist":[..],...}}}}</p>
 *
 * <p>校验码：CRC16-CCITT（多项式 0x1021、初值 0，查表算法），与慧种田
 * {@code CRC16M.doCrc} 一致；<b>不是</b> Modbus CRC16（0xA001/0xFFFF）。</p>
 */
public final class IflyTekValvePayloadUtil {

	/** 下行功能码 */
	private static final byte FUNC_DOWNLOAD = (byte) 0x81;

	/** 空闲心跳（秒） */
	private static final int IDLE_HEART_TIME = 30;

	/** 工作上报类型 */
	private static final int WORK_REPORT_TYPE = 0;

	/** 工作上报间隔（秒） */
	private static final int WORK_REPORT_INTERVAL = 120;

	private IflyTekValvePayloadUtil() {
	}

	/**
	 * 单阀 para_set 数据域 JSON
	 *
	 * @param taskNo 任务编码（8位数字，对应 para_ack 的 id）
	 * @param dir    动作方向：0关闭 1=A侧开启 2=B侧开启 3=AB全开
	 * @param open   开启角度 0-100
	 */
	public static String buildParaSetJson(String taskNo, int dir, int open) {
		JSONObject data = new JSONObject();
		data.put("dir", dir);
		data.put("open", open);
		return wrapParaSetJson(taskNo, data);
	}

	/**
	 * 多头阀（双头/三头/四头）para_set 数据域 JSON
	 *
	 * @param taskNo   任务编码
	 * @param openlist 各头开度列表 0-100
	 */
	public static String buildParaSetJson(String taskNo, List<Integer> openlist) {
		JSONObject data = new JSONObject();
		data.put("openlist", JSONArray.from(openlist));
		return wrapParaSetJson(taskNo, data);
	}

	private static String wrapParaSetJson(String taskNo, JSONObject data) {
		data.put("ctlmode", 0);
		data.put("idlehearttime", IDLE_HEART_TIME);
		data.put("work_reporttype", WORK_REPORT_TYPE);
		data.put("work_reportinter", WORK_REPORT_INTERVAL);
		//取消电动阀每隔10个小时自动停止（与慧种田一致）
		data.put("value_actinter", 0);
		data.put("max_open_time", 0);
		//慧种田同款取值：epoch秒*2/1e9 ≈ 3.58（历史兼容写法，设备会原样回显）
		data.put("batminvol", Instant.now().getEpochSecond() * 2 / 1000000000D);
		JSONObject body = new JSONObject();
		body.put("id", taskNo);
		body.put("data", data);
		JSONObject payload = new JSONObject();
		payload.put("para_set", body);
		return payload.toJSONString();
	}

	/**
	 * 帧封装：数据域 JSON → 完整帧 hex（AEP dataType=2 下发）
	 *
	 * @param sn   设备编号（≤15字符，不足右侧补0）
	 * @param json 数据域 JSON
	 */
	public static String wrapFrame(String sn, String json) {
		byte[] snBytes = sn.getBytes(StandardCharsets.US_ASCII);
		byte[] dataBytes = json.getBytes(StandardCharsets.UTF_8);
		byte[] frame = new byte[22 + dataBytes.length];
		frame[0] = FUNC_DOWNLOAD;
		System.arraycopy(snBytes, 0, frame, 1, Math.min(snBytes.length, 15));
		frame[16] = 0x01;
		frame[17] = 0x01;
		frame[18] = (byte) ((dataBytes.length >> 8) & 0xFF);
		frame[19] = (byte) (dataBytes.length & 0xFF);
		System.arraycopy(dataBytes, 0, frame, 20, dataBytes.length);
		int crc = crc16(frame, frame.length - 2);
		frame[frame.length - 2] = (byte) ((crc >> 8) & 0xFF);
		frame[frame.length - 1] = (byte) (crc & 0xFF);
		return HexUtils.bytesToHex(frame);
	}

	/**
	 * CRC16-CCITT：与慧种田 {@code CRC16M.doCrc} 完全一致
	 * （初值 0，{@code crc = (crc >> 8) ^ table[(crc ^ b) & 0xFF]}，
	 * 表与字节序已对上行帧 52DB/ADCD/7A2E 实测校验通过）
	 */
	static int crc16(byte[] arr, int len) {
		int crc = 0;
		for (int i = 0; i < len; i++) {
			crc = (crc >> 8) ^ CRC16_CCITT_TABLE[(crc ^ arr[i]) & 0xFF];
		}
		return crc;
	}

	/** 与慧种田 CRC16M.crc16_ccitt_table 相同的查表表 */
	private static final int[] CRC16_CCITT_TABLE = { 0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5,
			0x60c6, 0x70e7, 0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 0x1231,
			0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6, 0x9339, 0x8318, 0xb37b, 0xa35a,
			0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4,
			0x5485, 0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 0x3653, 0x2672,
			0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4, 0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df,
			0xe7fe, 0xd79d, 0xc7bc, 0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
			0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b, 0x5af5, 0x4ad4, 0x7ab7,
			0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12, 0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58,
			0xbb3b, 0xab1a, 0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41, 0xedae,
			0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49, 0x7e97, 0x6eb6, 0x5ed5, 0x4ef4,
			0x3e13, 0x2e32, 0x1e51, 0x0e70, 0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59,
			0x8f78, 0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f, 0x1080, 0x00a1,
			0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067, 0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d,
			0xd31c, 0xe37f, 0xf35e, 0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
			0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d, 0x34e2, 0x24c3, 0x14a0,
			0x0481, 0x7466, 0x6447, 0x5424, 0x4405, 0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e,
			0xc71d, 0xd73c, 0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634, 0xd94c,
			0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab, 0x5844, 0x4865, 0x7806, 0x6827,
			0x18c0, 0x08e1, 0x3882, 0x28a3, 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb,
			0xbb9a, 0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 0xfd2e, 0xed0f,
			0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9, 0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2,
			0x2c83, 0x1ce0, 0x0cc1, 0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
			0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };
}
