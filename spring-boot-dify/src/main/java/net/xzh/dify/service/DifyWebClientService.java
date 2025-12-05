package net.xzh.dify.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xzh.dify.dto.ChatMessageRequest;
import net.xzh.dify.dto.HeaderRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DifyWebClientService {

    private static final String CHAT_MESSAGES_ENDPOINT = "/chat-messages";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SSE_DATA_PREFIX = "data:";
    private static final String SSE_END_MARKER = "[DONE]";
    private static final String ERROR_KEY = "error";
    private static final String RAW_DATA_KEY = "raw_data";

    private final WebClient webClient;

    /**
     * 发送流式请求（返回数组）
     */
    public Flux<JSONObject> sendStreamingMessage(HeaderRequest header, ChatMessageRequest chatMessage) {
        chatMessage.setUser(header.getUser_id());
        return webClient.post()
                .uri(CHAT_MESSAGES_ENDPOINT)
                .header(AUTHORIZATION_HEADER, header.getApi_key())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(chatMessage)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::parseSseData)
                .filter(Objects::nonNull);
    }

    /**
     * 发送阻塞式请求（返回单个对象）
     */
    public Mono<JSONObject> sendBlockingMessage(HeaderRequest header, ChatMessageRequest chatMessage) {
        chatMessage.setUser(header.getUser_id());
        return webClient.post()
                .uri(CHAT_MESSAGES_ENDPOINT)
                .header(AUTHORIZATION_HEADER, header.getApi_key())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(chatMessage)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseBlockingResponse);
    }

    /**
     * 解析阻塞式响应
     */
    private Mono<JSONObject> parseBlockingResponse(String response) {
        try {
            Object parsed = JSON.parse(response);
            return parseJsonToObject(parsed);
        } catch (Exception e) {
            log.error("解析JSON响应失败，响应内容: {}", response, e);
            return Mono.error(new IllegalArgumentException("响应解析失败: " + e.getMessage(), e));
        }
    }

    /**
     * 将解析后的JSON对象转换为Mono<JSONObject>
     */
    private Mono<JSONObject> parseJsonToObject(Object parsed) {
        if (parsed instanceof JSONArray) {
            JSONArray array = (JSONArray) parsed;
            if (!array.isEmpty()) {
                return Mono.just(array.getJSONObject(0));
            } else {
                log.warn("响应JSON数组为空");
                return Mono.empty();
            }
        } else if (parsed instanceof JSONObject) {
            return Mono.just((JSONObject) parsed);
        } else {
            log.error("无法识别的响应格式: {}", parsed.getClass().getName());
            return Mono.error(new IllegalArgumentException("无法识别的响应格式"));
        }
    }

    /**
     * 解析SSE数据行
     */
    private JSONObject parseSseData(String line) {
        try {
            if (line.startsWith(SSE_DATA_PREFIX)) {
                String jsonStr = line.substring(SSE_DATA_PREFIX.length()).trim();

                if (jsonStr.isEmpty() || SSE_END_MARKER.equals(jsonStr)) {
                    log.debug("SSE流结束");
                    return null;
                }

                return parseJsonObject(jsonStr);
            } else {
                return parseJsonObject(line);
            }
        } catch (Exception e) {
            log.error("解析SSE数据失败，原始数据: {}", truncateLongString(line), e);
            return createErrorJsonObject("SSE数据解析失败", line, e);
        }
    }

    /**
     * 解析JSON字符串为JSONObject
     */
    private JSONObject parseJsonObject(String jsonStr) {
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            log.trace("解析到JSON对象: {}", jsonObject);
            return jsonObject;
        } catch (Exception e) {
            log.error("JSON解析失败，内容: {}", truncateLongString(jsonStr), e);
            return createErrorJsonObject("JSON解析失败", jsonStr, e);
        }
    }

    /**
     * 创建错误JSON对象
     */
    private JSONObject createErrorJsonObject(String message, String rawData, Exception e) {
        JSONObject errorJson = new JSONObject();
        errorJson.put(ERROR_KEY, message);
        errorJson.put("exception", e.getMessage());
        errorJson.put(RAW_DATA_KEY, truncateLongString(rawData));
        return errorJson;
    }

    /**
     * 截断长字符串，避免日志过大
     */
    private String truncateLongString(String str) {
        final int MAX_LENGTH = 500;
        if (str != null && str.length() > MAX_LENGTH) {
            return str.substring(0, MAX_LENGTH) + "...[" + (str.length() - MAX_LENGTH) + " more chars]";
        }
        return str;
    }
}