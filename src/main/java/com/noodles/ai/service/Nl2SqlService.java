package com.noodles.ai.service;

import com.noodles.ai.dto.Nl2SqlRequest;
import com.noodles.ai.dto.Nl2SqlResponse;

/**
 * NL2SQL 生成服务。
 *
 * @author Noodles
 * @date 2026/3/26 15:10
 */
public interface Nl2SqlService {

  /**
   * 根据自然语言请求生成查询 SQL。
   *
   * @param request NL2SQL 请求参数
   * @return 生成结果
   */
  Nl2SqlResponse generateSql(Nl2SqlRequest request);

}
