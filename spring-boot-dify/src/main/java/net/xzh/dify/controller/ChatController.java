package net.xzh.dify.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;

import net.xzh.dify.annotation.RequestHeader;
import net.xzh.dify.dto.ChatMessageRequest;
import net.xzh.dify.dto.HeaderRequest;
import net.xzh.dify.service.DifyWebClientService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 调用 Dify API 对话
 */
@RestController
@RequestMapping("/api/v1")
public class ChatController {

	@Autowired
	private DifyWebClientService difyWebClientService;

	/**
	 * 流式响应端点
	 * 
	 * @param headerRequest
	 * @param chatMessage
	 * @return
	 */
	@PostMapping(value = "/chat-messages/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<JSONObject> streamChatMessages(@RequestHeader HeaderRequest header,
			@Valid @RequestBody ChatMessageRequest chatMessage) {
		chatMessage.setResponse_mode("streaming");
		return difyWebClientService.sendStreamingMessage(header, chatMessage);
	}

	/**
	 * 阻塞式响应端点
	 * 
	 * @param headerRequest
	 * @param chatMessage
	 * @return
	 */
	@PostMapping(value = "/chat-messages", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<JSONObject> blockChatMessages(@RequestHeader HeaderRequest header,
			@Valid @RequestBody ChatMessageRequest chatMessage) {
		chatMessage.setResponse_mode("blocking");
		return difyWebClientService.sendBlockingMessage(header, chatMessage);
	}
}