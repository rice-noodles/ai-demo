package com.noodles.ai.service;

import java.util.List;

/**
 * 数据库结构读取服务。
 *
 * @author Noodles
 * @date 2026/3/26 15:12
 */
public interface DatabaseSchemaService {

  /**
   * 读取数据库表结构描述。
   *
   * @param includeTables 指定读取的表，空表示读取全部
   * @return 可用于提示词的结构描述
   */
  String loadSchemaDescription(List<String> includeTables);

}
