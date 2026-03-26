package com.noodles.ai.constant;

/**
 * @author Noodles
 * @date 2026/3/26 10:38
 */
public final class ChatConstant {

  public static final String MESSAGE_PARAM = "message";

  public static final String UNKNOWN_MODEL = "unknown";

  public static final String METRIC_CHAT_REQUESTS = "ai.chat.requests";
  public static final String METRIC_CHAT_REQUEST_DURATION = "ai.chat.request.duration";
  public static final String METRIC_CHAT_TOKENS = "ai.chat.tokens";
  public static final String METRIC_CHAT_COST_USD = "ai.chat.cost.usd";

  public static final String TAG_MODEL = "model";
  public static final String TAG_STATUS = "status";
  public static final String TAG_TYPE = "type";

  public static final String STATUS_SUCCESS = "success";
  public static final String STATUS_ERROR = "error";

  public static final String TOKEN_TYPE_INPUT = "input";
  public static final String TOKEN_TYPE_OUTPUT = "output";
  public static final String TOKEN_TYPE_TOTAL = "total";

  private ChatConstant() {
  }

}
