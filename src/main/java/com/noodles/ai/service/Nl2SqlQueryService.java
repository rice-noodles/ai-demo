package com.noodles.ai.service;

import com.noodles.ai.dto.Nl2SqlExecuteRequest;
import com.noodles.ai.dto.Nl2SqlExecuteResponse;

/**
 * @author Noodles
 * @date 2026/3/26 12:10
 */
public interface Nl2SqlQueryService {
  /**
   * 生成 SQL 并执行
   *
   * @param request 请求参数
   * @return 执行结果
   */
  Nl2SqlExecuteResponse generateAndExecute(Nl2SqlExecuteRequest request);

}
