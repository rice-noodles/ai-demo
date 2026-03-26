package com.noodles.ai.controller;

import com.noodles.ai.component.ChatMetricsRecorder;
import com.noodles.ai.constant.ChatConstant;
import com.noodles.ai.util.ChatResponseUtils;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Noodles
 * @date 2026/3/25 14:17
 */
@AllArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

  private final ChatClient chatClient;
  private final ChatMetricsRecorder chatMetricsRecorder;

  @GetMapping
  public String chat(@RequestParam(ChatConstant.MESSAGE_PARAM) String message) {
    long startNanos = System.nanoTime();
    String model = ChatConstant.UNKNOWN_MODEL;

    try {
      ChatResponse response = chatClient
          .prompt()
          .user(message)
          .call()
          .chatResponse();

      ChatResponseMetadata metadata = response.getMetadata();
      model = ChatResponseUtils.resolveModel(metadata);

      chatMetricsRecorder.recordSuccess(model, metadata != null ? metadata.getUsage() : null);

      return ChatResponseUtils.extractContent(response);
    } catch (RuntimeException ex) {
      chatMetricsRecorder.recordError(model);
      throw ex;
    } finally {
      chatMetricsRecorder.recordDuration(model, System.nanoTime() - startNanos);
    }
  }

}
