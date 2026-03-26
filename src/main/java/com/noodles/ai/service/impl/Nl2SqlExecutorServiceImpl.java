package com.noodles.ai.service.impl;

import com.noodles.ai.constant.Nl2SqlConstant;
import com.noodles.ai.mapper.AiSqlMapper;
import com.noodles.ai.service.Nl2SqlExecutorService;
import com.noodles.ai.service.Nl2SqlResultMapper;
import com.noodles.ai.util.SqlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NL2SQL 执行服务实现。
 * 负责 SQL 校验、限流与结果映射。
 *
 * @author Noodles
 * @date 2026/3/26 15:13
 */
@Service
public class Nl2SqlExecutorServiceImpl implements Nl2SqlExecutorService {

  private final AiSqlMapper aiSqlMapper;
  private final Nl2SqlResultMapper resultMapper;
  private final int defaultMaxRows;

  public Nl2SqlExecutorServiceImpl(AiSqlMapper aiSqlMapper,
                                   Nl2SqlResultMapper resultMapper,
                                   @Value("${nl2sql.default-max-rows:200}") int defaultMaxRows) {
    this.aiSqlMapper = aiSqlMapper;
    this.resultMapper = resultMapper;
    this.defaultMaxRows = defaultMaxRows;
  }

  /**
   * 执行 AI 生成的 SQL 并返回结构化结果。
   *
   * @param sql           AI 生成 SQL
   * @param resultType    目标结果类型
   * @param maxRows       最大返回行数
   * @param allowedTables 允许访问表
   * @return 执行结果
   */
  @Override
  public ExecuteResult executeQuery(String sql, String resultType, Integer maxRows, List<String> allowedTables) {
    Set<String> tableWhitelist = SqlUtils.normalizeTableWhitelist(allowedTables);
    SqlUtils.validateExecutableQuery(sql, tableWhitelist);

    int limitedRows = normalizeMaxRows(maxRows);
    String executableSql = SqlUtils.ensureLimit(sql, limitedRows);
    List<Map<String, Object>> rawRows = aiSqlMapper.executeQuery(executableSql);
    List<?> mappedRows = resultMapper.mapRows(rawRows, resultType);

    return new ExecuteResult(
        executableSql,
        resultMapper.normalizeResultType(resultType),
        mappedRows == null ? Collections.emptyList() : mappedRows
    );
  }

  private int normalizeMaxRows(Integer maxRows) {
    if (maxRows == null || maxRows <= 0) {
      return defaultMaxRows > 0 ? defaultMaxRows : Nl2SqlConstant.DEFAULT_MAX_ROWS;
    }
    return maxRows;
  }

}
