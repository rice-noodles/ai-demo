package com.noodles.ai.service;

import java.util.List;

/**
 * NL2SQL 执行服务。
 *
 * @author Noodles
 * @date 2026/3/26 15:13
 */
public interface Nl2SqlExecutorService {

  /**
   * 执行 AI 生成的 SQL 并返回结构化结果。
   *
   * @param sql           AI 生成 SQL
   * @param resultType    目标结果类型
   * @param maxRows       最大返回行数
   * @param allowedTables 允许访问表
   * @return 执行结果
   */
  ExecuteResult executeQuery(String sql, String resultType, Integer maxRows, List<String> allowedTables);

  /**
   * SQL 执行结果。
   *
   * @param sql        实际执行 SQL
   * @param resultType 返回类型
   * @param rows       返回行
   */
  record ExecuteResult(String sql, String resultType, List<?> rows) {
  }

}
