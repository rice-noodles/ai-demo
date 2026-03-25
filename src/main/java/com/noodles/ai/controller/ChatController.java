package com.noodles.ai.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author jacky_huang
 * @date 2026/3/25 14:17
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

  private final ChatClient chatClient;
  private final MeterRegistry meterRegistry;
  private final double inputPricePer1kTokensUsd;
  private final double outputPricePer1kTokensUsd;

  public ChatController(ChatClient.Builder builder,
      MeterRegistry meterRegistry,
      @Value("${monitoring.ai.pricing.input-per-1k-tokens-usd:0.0}") double inputPricePer1kTokensUsd,
      @Value("${monitoring.ai.pricing.output-per-1k-tokens-usd:0.0}") double outputPricePer1kTokensUsd) {
    this.chatClient = builder.build();
    this.meterRegistry = meterRegistry;
    this.inputPricePer1kTokensUsd = inputPricePer1kTokensUsd;
    this.outputPricePer1kTokensUsd = outputPricePer1kTokensUsd;
  }

  @GetMapping
  public String chat(@RequestParam("message") String message) {
    long startNanos = System.nanoTime();
    String model = "unknown";

    try {
      ChatResponse response = chatClient
        .prompt()
        .user(message)
        .call()
        .chatResponse();

      ChatResponseMetadata metadata = response.getMetadata();
      if (metadata != null && metadata.getModel() != null && !metadata.getModel().isBlank()) {
        model = metadata.getModel();
      }

      meterRegistry.counter("ai.chat.requests", "model", model, "status", "success").increment();
      recordUsageMetrics(model, metadata != null ? metadata.getUsage() : null);

      return extractResponseContent(response);
    } catch (RuntimeException ex) {
      meterRegistry.counter("ai.chat.requests", "model", model, "status", "error").increment();
      throw ex;
    } finally {
      long elapsedNanos = System.nanoTime() - startNanos;
      meterRegistry.timer("ai.chat.request.duration", "model", model)
          .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
  }

  private void recordUsageMetrics(String model, Usage usage) {
    if (usage == null) {
      return;
    }

    int inputTokens = normalizeTokenCount(usage.getPromptTokens());
    int outputTokens = normalizeTokenCount(usage.getCompletionTokens());
    int totalTokens = normalizeTokenCount(usage.getTotalTokens());

    if (inputTokens > 0) {
      meterRegistry.counter("ai.chat.tokens", "model", model, "type", "input").increment(inputTokens);
    }
    if (outputTokens > 0) {
      meterRegistry.counter("ai.chat.tokens", "model", model, "type", "output").increment(outputTokens);
    }
    if (totalTokens > 0) {
      meterRegistry.counter("ai.chat.tokens", "model", model, "type", "total").increment(totalTokens);
    }

    double requestCostUsd = (inputTokens / 1000.0d) * inputPricePer1kTokensUsd
        + (outputTokens / 1000.0d) * outputPricePer1kTokensUsd;
    if (requestCostUsd > 0) {
      meterRegistry.counter("ai.chat.cost.usd", "model", model).increment(requestCostUsd);
    }
  }

  private int normalizeTokenCount(Integer tokenCount) {
    return tokenCount == null || tokenCount < 0 ? 0 : tokenCount;
  }

  private String extractResponseContent(ChatResponse response) {
    if (response.getResult() == null || response.getResult().getOutput() == null) {
      return "";
    }

    String content = response.getResult().getOutput().getText();
    return content == null ? "" : content;
  }

}
