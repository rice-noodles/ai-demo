package com.noodles.ai.service.impl;

import com.noodles.ai.service.DatabaseSchemaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 数据库结构读取服务实现。
 *
 * @author Noodles
 * @date 2026/3/26 15:12
 */
@Service
public class DatabaseSchemaServiceImpl implements DatabaseSchemaService {

  private final DataSource dataSource;
  private final int schemaTableLimit;
  private final int schemaColumnLimit;

  public DatabaseSchemaServiceImpl(DataSource dataSource,
                                   @Value("${nl2sql.schema-table-limit:30}") int schemaTableLimit,
                                   @Value("${nl2sql.schema-column-limit:50}") int schemaColumnLimit) {
    this.dataSource = dataSource;
    this.schemaTableLimit = schemaTableLimit;
    this.schemaColumnLimit = schemaColumnLimit;
  }

  /**
   * 读取数据库表结构描述。
   *
   * @param includeTables 指定读取的表，空表示读取全部
   * @return 可用于提示词的结构描述
   */
  @Override
  public String loadSchemaDescription(List<String> includeTables) {
    Set<String> includeTableSet = normalizeTables(includeTables);
    StringBuilder sb = new StringBuilder();

    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      String catalog = connection.getCatalog();
      int tableCount = 0;

      try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE", "VIEW"})) {
        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          if (!shouldIncludeTable(includeTableSet, tableName)) {
            continue;
          }
          if (tableCount >= schemaTableLimit) {
            break;
          }

          sb.append("表 ").append(tableName).append(":\n");
          appendColumns(sb, metaData, catalog, tableName);
          sb.append('\n');
          tableCount++;
        }
      }
    } catch (SQLException ex) {
      throw new IllegalStateException("读取数据库元数据失败", ex);
    }

    if (sb.isEmpty()) {
      if (CollectionUtils.isEmpty(includeTables)) {
        return "数据库中未读取到可用表结构。";
      }
      return "未读取到指定表结构：" + String.join(", ", includeTables);
    }
    return sb.toString().trim();
  }

  private void appendColumns(StringBuilder sb, DatabaseMetaData metaData, String catalog, String tableName)
      throws SQLException {
    int columnCount = 0;
    try (ResultSet columns = metaData.getColumns(catalog, null, tableName, "%")) {
      while (columns.next()) {
        if (columnCount >= schemaColumnLimit) {
          sb.append("  - ...\n");
          break;
        }
        String columnName = columns.getString("COLUMN_NAME");
        String typeName = columns.getString("TYPE_NAME");
        String nullable = columns.getString("IS_NULLABLE");
        sb.append("  - ")
            .append(columnName)
            .append(" ")
            .append(typeName)
            .append(" ")
            .append("NULLABLE=")
            .append(nullable == null ? "UNKNOWN" : nullable)
            .append('\n');
        columnCount++;
      }
    }
  }

  private boolean shouldIncludeTable(Set<String> includeTableSet, String tableName) {
    if (includeTableSet.isEmpty()) {
      return true;
    }
    return includeTableSet.contains(normalizeTable(tableName));
  }

  private Set<String> normalizeTables(List<String> tables) {
    if (CollectionUtils.isEmpty(tables)) {
      return Set.of();
    }
    Set<String> set = new HashSet<>();
    for (String table : tables) {
      if (StringUtils.hasText(table)) {
        set.add(normalizeTable(table));
      }
    }
    return set;
  }

  private String normalizeTable(String table) {
    return table.trim().toLowerCase(Locale.ROOT);
  }

}
