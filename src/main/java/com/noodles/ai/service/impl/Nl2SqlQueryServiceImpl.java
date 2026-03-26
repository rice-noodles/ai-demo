package com.noodles.ai.service.impl;

import com.noodles.ai.dto.Nl2SqlExecuteRequest;
import com.noodles.ai.dto.Nl2SqlExecuteResponse;
import com.noodles.ai.dto.Nl2SqlRequest;
import com.noodles.ai.dto.Nl2SqlResponse;
import com.noodles.ai.service.Nl2SqlExecutorService;
import com.noodles.ai.service.Nl2SqlQueryService;
import com.noodles.ai.service.Nl2SqlService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * NL2SQL 查询编排服务实现。
 * 负责串联 SQL 生成与执行流程。
 *
 * @author Noodles
 * @date 2026/3/26 14:09
 */
@AllArgsConstructor
@Service
public class Nl2SqlQueryServiceImpl implements Nl2SqlQueryService {

  private final Nl2SqlService nl2SqlService;
  private final Nl2SqlExecutorService nl2SqlExecutorService;

  /**
   * 生成 SQL 并执行。
   *
   * @param request 请求参数
   * @return 执行结果
   */
  @Override
  public Nl2SqlExecuteResponse generateAndExecute(Nl2SqlExecuteRequest request) {
    Nl2SqlResponse generated = nl2SqlService.generateSql(new Nl2SqlRequest(
        request.question(),
        request.schema(),
        request.dialect(),
        request.additionalRules(),
        request.tables()
    ));

    Nl2SqlExecutorService.ExecuteResult executeResult = nl2SqlExecutorService.executeQuery(
        generated.sql(),
        request.resultType(),
        request.maxRows(),
        request.tables()
    );

    return new Nl2SqlExecuteResponse(
        executeResult.sql(),
        generated.model(),
        executeResult.resultType(),
        executeResult.rows().size(),
        executeResult.rows()
    );
  }

}
