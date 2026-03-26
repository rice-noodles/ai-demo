package com.noodles.ai.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Noodles
 * @date 2026/3/26 11:17
 */
public final class SqlUtils {

  private static final Pattern FORBIDDEN_SQL_PATTERN = Pattern.compile(
      "\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke|merge|replace|call|execute)\\b",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern FROM_JOIN_TABLE_PATTERN = Pattern.compile(
      "\\b(from|join)\\s+([`\"\\[]?[a-zA-Z0-9_$.]+[`\"\\]]?)",
      Pattern.CASE_INSENSITIVE);

  private SqlUtils() {
  }

  public static String sanitizeGeneratedSql(String rawSql) {
    if (rawSql == null) {
      return "";
    }

    String sql = rawSql.trim();
    if (sql.startsWith("```")) {
      sql = removeMarkdownFence(sql);
    }

    if (sql.regionMatches(true, 0, "sql:", 0, 4)) {
      sql = sql.substring(4).trim();
    }

    return sql;
  }

  public static boolean isReadOnlyQuery(String sql) {
    if (sql == null || sql.isBlank()) {
      return false;
    }

    String normalized = removeLeadingComments(sql).trim().toLowerCase(Locale.ROOT);
    return normalized.startsWith("select")
        || normalized.startsWith("with")
        || normalized.startsWith("show")
        || normalized.startsWith("desc")
        || normalized.startsWith("describe")
        || normalized.startsWith("explain");
  }

  public static void validateExecutableQuery(String sql, Set<String> tableWhitelist) {
    if (!isReadOnlyQuery(sql)) {
      throw new IllegalStateException("仅允许执行只读查询 SQL");
    }

    String normalizedSql = sanitizeGeneratedSql(sql).trim();
    if (containsMultipleStatements(normalizedSql)) {
      throw new IllegalStateException("仅支持执行单条 SQL");
    }

    if (FORBIDDEN_SQL_PATTERN.matcher(normalizedSql).find()) {
      throw new IllegalStateException("SQL 包含禁止关键字");
    }

    if (!CollectionUtils.isEmpty(tableWhitelist)) {
      Set<String> tablesInSql = extractTableNames(normalizedSql);
      for (String tableInSql : tablesInSql) {
        if (!tableWhitelist.contains(normalizeTableName(tableInSql))) {
          throw new IllegalStateException("SQL 访问了未授权表: " + tableInSql);
        }
      }
    }
  }

  public static Set<String> normalizeTableWhitelist(java.util.List<String> tables) {
    if (CollectionUtils.isEmpty(tables)) {
      return Set.of();
    }

    Set<String> normalized = new HashSet<>();
    for (String table : tables) {
      if (StringUtils.hasText(table)) {
        normalized.add(normalizeTableName(table));
      }
    }
    return normalized;
  }

  public static String ensureLimit(String sql, int maxRows) {
    String trimmed = sanitizeGeneratedSql(sql).trim();
    if (maxRows <= 0) {
      return trimmed;
    }
    if (Pattern.compile("\\blimit\\s+\\d+\\b", Pattern.CASE_INSENSITIVE).matcher(trimmed).find()) {
      return trimmed;
    }
    if (trimmed.endsWith(";")) {
      return trimmed.substring(0, trimmed.length() - 1) + " LIMIT " + maxRows + ";";
    }
    return trimmed + " LIMIT " + maxRows;
  }

  private static String removeMarkdownFence(String sql) {
    String trimmed = sql.trim();
    if (!trimmed.startsWith("```")) {
      return trimmed;
    }

    int firstLineBreak = trimmed.indexOf('\n');
    if (firstLineBreak < 0) {
      return trimmed.replace("```", "").trim();
    }

    String body = trimmed.substring(firstLineBreak + 1);
    int lastFence = body.lastIndexOf("```");
    if (lastFence >= 0) {
      body = body.substring(0, lastFence);
    }
    return body.trim();
  }

  private static String removeLeadingComments(String sql) {
    String remaining = sql.trim();
    boolean changed = true;

    while (changed && !remaining.isEmpty()) {
      changed = false;

      if (remaining.startsWith("--")) {
        int nextLine = remaining.indexOf('\n');
        if (nextLine < 0) {
          return "";
        }
        remaining = remaining.substring(nextLine + 1).trim();
        changed = true;
      } else if (remaining.startsWith("/*")) {
        int end = remaining.indexOf("*/");
        if (end < 0) {
          return "";
        }
        remaining = remaining.substring(end + 2).trim();
        changed = true;
      }
    }

    return remaining;
  }

  private static boolean containsMultipleStatements(String sql) {
    String noSemicolon = sql.replaceAll(";\\s*$", "");
    return noSemicolon.contains(";");
  }

  private static Set<String> extractTableNames(String sql) {
    Set<String> tables = new HashSet<>();
    Matcher matcher = FROM_JOIN_TABLE_PATTERN.matcher(sql);
    while (matcher.find()) {
      tables.add(matcher.group(2));
    }
    return tables;
  }

  private static String normalizeTableName(String tableName) {
    String normalized = tableName.trim().toLowerCase(Locale.ROOT);
    normalized = normalized.replace("`", "")
        .replace("\"", "")
        .replace("[", "")
        .replace("]", "");
    int dotIndex = normalized.lastIndexOf('.');
    if (dotIndex >= 0 && dotIndex < normalized.length() - 1) {
      return normalized.substring(dotIndex + 1);
    }
    return normalized;
  }

}
