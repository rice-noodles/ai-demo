package com.noodles.ai.util;

import com.noodles.ai.constant.ChatConstant;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * @author Noodles
 * @date 2026/3/26 10:40
 */
public class ChatResponseUtils {

  private ChatResponseUtils() {
  }

  public static String resolveModel(ChatResponseMetadata metadata) {
    if (metadata != null && metadata.getModel() != null && !metadata.getModel().isBlank()) {
      return metadata.getModel();
    }
    return ChatConstant.UNKNOWN_MODEL;
  }

  public static String extractContent(ChatResponse response) {
    if (response.getResult() == null || response.getResult().getOutput() == null) {
      return "";
    }

    String content = response.getResult().getOutput().getText();
    return content == null ? "" : content;
  }

  public static int normalizeTokenCount(Integer tokenCount) {
    return tokenCount == null || tokenCount < 0 ? 0 : tokenCount;
  }

}
