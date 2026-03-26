package com.noodles.ai.constant;

/**
 * @author Noodles
 * @date 2026/3/26 11:15
 */
public final class Nl2SqlConstant {

  public static final String RESULT_TYPE_MAP = "map";

  public static final int DEFAULT_MAX_ROWS = 200;
  public static final String DEFAULT_DIALECT = "MySQL";
  public static final String DEFAULT_SCHEMA_HINT = "未提供数据库结构，请基于通用字段命名习惯生成查询 SQL。";
  public static final String DEFAULT_RULES = "仅生成查询 SQL，不要生成 INSERT/UPDATE/DELETE/DDL。";

  private Nl2SqlConstant() {
  }

}
