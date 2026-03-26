package com.noodles.ai.component;

import com.noodles.ai.constant.ChatConstant;
import com.noodles.ai.util.ChatResponseUtils;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ChatMetricsRecorder {

  private final MeterRegistry meterRegistry;
  private final double inputPricePer1kTokensUsd;
  private final double outputPricePer1kTokensUsd;

  public ChatMetricsRecorder(MeterRegistry meterRegistry,
                             @Value("${monitoring.ai.pricing.input-per-1k-tokens-usd:0.0}") double inputPricePer1kTokensUsd,
                             @Value("${monitoring.ai.pricing.output-per-1k-tokens-usd:0.0}") double outputPricePer1kTokensUsd) {
    this.meterRegistry = meterRegistry;
    this.inputPricePer1kTokensUsd = inputPricePer1kTokensUsd;
    this.outputPricePer1kTokensUsd = outputPricePer1kTokensUsd;
  }

  public void recordSuccess(String model, Usage usage) {
    meterRegistry.counter(ChatConstant.METRIC_CHAT_REQUESTS,
        ChatConstant.TAG_MODEL, model,
        ChatConstant.TAG_STATUS, ChatConstant.STATUS_SUCCESS).increment();
    recordUsageMetrics(model, usage);
  }

  public void recordError(String model) {
    meterRegistry.counter(ChatConstant.METRIC_CHAT_REQUESTS,
        ChatConstant.TAG_MODEL, model,
        ChatConstant.TAG_STATUS, ChatConstant.STATUS_ERROR).increment();
  }

  public void recordDuration(String model, long elapsedNanos) {
    meterRegistry.timer(ChatConstant.METRIC_CHAT_REQUEST_DURATION,
            ChatConstant.TAG_MODEL, model)
        .record(elapsedNanos, TimeUnit.NANOSECONDS);
  }

  private void recordUsageMetrics(String model, Usage usage) {
    if (usage == null) {
      return;
    }

    int inputTokens = ChatResponseUtils.normalizeTokenCount(usage.getPromptTokens());
    int outputTokens = ChatResponseUtils.normalizeTokenCount(usage.getCompletionTokens());
    int totalTokens = ChatResponseUtils.normalizeTokenCount(usage.getTotalTokens());

    if (inputTokens > 0) {
      meterRegistry.counter(ChatConstant.METRIC_CHAT_TOKENS,
              ChatConstant.TAG_MODEL, model,
              ChatConstant.TAG_TYPE, ChatConstant.TOKEN_TYPE_INPUT)
          .increment(inputTokens);
    }
    if (outputTokens > 0) {
      meterRegistry.counter(ChatConstant.METRIC_CHAT_TOKENS,
              ChatConstant.TAG_MODEL, model,
              ChatConstant.TAG_TYPE, ChatConstant.TOKEN_TYPE_OUTPUT)
          .increment(outputTokens);
    }
    if (totalTokens > 0) {
      meterRegistry.counter(ChatConstant.METRIC_CHAT_TOKENS,
              ChatConstant.TAG_MODEL, model,
              ChatConstant.TAG_TYPE, ChatConstant.TOKEN_TYPE_TOTAL)
          .increment(totalTokens);
    }

    double requestCostUsd = (inputTokens / 1000.0d) * inputPricePer1kTokensUsd
        + (outputTokens / 1000.0d) * outputPricePer1kTokensUsd;
    if (requestCostUsd > 0) {
      meterRegistry.counter(ChatConstant.METRIC_CHAT_COST_USD,
              ChatConstant.TAG_MODEL, model)
          .increment(requestCostUsd);
    }
  }

}
