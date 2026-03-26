package com.noodles.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noodles.ai.bean.Nl2SqlMapRow;
import com.noodles.ai.constant.Nl2SqlConstant;
import com.noodles.ai.service.Nl2SqlResultMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 查询结果映射服务实现。
 * 支持 map 结果和自定义 Bean 映射。
 *
 * @author Noodles
 * @date 2026/3/26 15:14
 */
@Service
public class Nl2SqlResultMapperImpl implements Nl2SqlResultMapper {

  private static final String SAFE_BEAN_PACKAGE = "com.noodles.ai.bean.";

  private final ObjectMapper objectMapper;

  public Nl2SqlResultMapperImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * 将 SQL 原始结果映射为目标类型。
   *
   * @param rows       数据库原始结果
   * @param resultType 目标类型
   * @return 映射后的结果
   */
  @Override
  public List<?> mapRows(List<Map<String, Object>> rows, String resultType) {
    Class<?> targetClass = resolveTargetClass(resultType);
    List<Map<String, Object>> normalizedRows = rows.stream().map(this::normalizeColumnKeys).toList();
    if (targetClass == Nl2SqlMapRow.class) {
      return normalizedRows.stream().map(Nl2SqlMapRow::new).toList();
    }
    return normalizedRows.stream().map(row -> objectMapper.convertValue(row, targetClass)).toList();
  }

  /**
   * 规范化返回类型。
   *
   * @param resultType 原始类型
   * @return 规范化类型
   */
  @Override
  public String normalizeResultType(String resultType) {
    if (!StringUtils.hasText(resultType)) {
      return Nl2SqlConstant.RESULT_TYPE_MAP;
    }
    return resultType.trim();
  }

  private Class<?> resolveTargetClass(String resultType) {
    String normalizedType = normalizeResultType(resultType);
    if (Nl2SqlConstant.RESULT_TYPE_MAP.equalsIgnoreCase(normalizedType)) {
      return Nl2SqlMapRow.class;
    }

    String className = normalizedType.contains(".")
        ? normalizedType
        : SAFE_BEAN_PACKAGE + normalizedType;
    if (!className.startsWith(SAFE_BEAN_PACKAGE)) {
      throw new IllegalArgumentException("resultType 仅允许使用 com.noodles.ai.bean 包下的类");
    }

    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("resultType 对应的 Bean 不存在: " + className, ex);
    }
  }

  private Map<String, Object> normalizeColumnKeys(Map<String, Object> row) {
    Map<String, Object> normalized = new LinkedHashMap<>();
    row.forEach((key, value) -> normalized.put(toCamelCase(key), value));
    return normalized;
  }

  private String toCamelCase(String input) {
    if (!StringUtils.hasText(input)) {
      return input;
    }

    String lower = input.trim().toLowerCase(Locale.ROOT);
    StringBuilder sb = new StringBuilder(lower.length());
    boolean upperNext = false;
    for (char c : lower.toCharArray()) {
      if (c == '_' || c == '-' || c == ' ') {
        upperNext = true;
        continue;
      }
      if (upperNext) {
        sb.append(Character.toUpperCase(c));
        upperNext = false;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
