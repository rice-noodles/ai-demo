package com.noodles.ai.service;

import java.util.List;
import java.util.Map;

/**
 * 查询结果映射服务。
 *
 * @author Noodles
 * @date 2026/3/26 15:14
 */
public interface Nl2SqlResultMapper {

  /**
   * 将 SQL 原始结果映射为目标类型。
   *
   * @param rows       数据库原始结果
   * @param resultType 目标类型
   * @return 映射后的结果
   */
  List<?> mapRows(List<Map<String, Object>> rows, String resultType);

  /**
   * 规范化返回类型。
   *
   * @param resultType 原始类型
   * @return 规范化类型
   */
  String normalizeResultType(String resultType);

}
