package com.noodles.ai.service.impl;

import com.noodles.ai.constant.Nl2SqlConstant;
import com.noodles.ai.dto.Nl2SqlRequest;
import com.noodles.ai.dto.Nl2SqlResponse;
import com.noodles.ai.service.DatabaseSchemaService;
import com.noodles.ai.service.Nl2SqlService;
import com.noodles.ai.util.ChatResponseUtils;
import com.noodles.ai.util.SqlUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * NL2SQL 生成服务实现。
 * 负责拼装提示词、调用大模型并校验返回 SQL 的可执行性。
 *
 * @author Noodles
 * @date 2026/3/26 15:11
 */
@Service
public class Nl2SqlServiceImpl implements Nl2SqlService {

  private final ChatClient chatClient;
  private final DatabaseSchemaService databaseSchemaService;
  private final String defaultDialect;
  private final String defaultSchema;
  private final String defaultRules;
  private final boolean autoReadSchema;

  public Nl2SqlServiceImpl(ChatClient.Builder builder,
                           DatabaseSchemaService databaseSchemaService,
                           @Value("${nl2sql.default-dialect:MySQL}") String defaultDialect,
                           @Value("${nl2sql.default-schema:}") String defaultSchema,
                           @Value("${nl2sql.default-rules:}") String defaultRules,
                           @Value("${nl2sql.auto-read-schema:true}") boolean autoReadSchema) {
    this.chatClient = builder.build();
    this.databaseSchemaService = databaseSchemaService;
    this.defaultDialect = defaultDialect;
    this.defaultSchema = defaultSchema;
    this.defaultRules = defaultRules;
    this.autoReadSchema = autoReadSchema;
  }

  /**
   * 根据自然语言请求生成查询 SQL。
   *
   * @param request NL2SQL 请求参数
   * @return 生成结果
   */
  @Override
  public Nl2SqlResponse generateSql(Nl2SqlRequest request) {
    if (request == null || !StringUtils.hasText(request.question())) {
      throw new IllegalArgumentException("question 不能为空");
    }

    String question = request.question().trim();
    String dialect = resolveDialect(request.dialect());
    String schema = resolveSchema(request.schema(), request.tables());
    String rules = resolveRules(request.additionalRules());

    ChatResponse response = chatClient
        .prompt()
        .system(buildSystemPrompt(dialect))
        .user(buildUserPrompt(question, dialect, schema, rules, request.tables()))
        .call()
        .chatResponse();

    String sql = SqlUtils.sanitizeGeneratedSql(ChatResponseUtils.extractContent(response));
    if (!SqlUtils.isReadOnlyQuery(sql)) {
      throw new IllegalStateException("模型未返回有效查询 SQL，请补充更明确的查询需求后重试");
    }

    return new Nl2SqlResponse(sql, ChatResponseUtils.resolveModel(response.getMetadata()));
  }

  private String resolveDialect(String dialect) {
    if (StringUtils.hasText(dialect)) {
      return dialect.trim();
    }
    if (StringUtils.hasText(defaultDialect)) {
      return defaultDialect.trim();
    }
    return Nl2SqlConstant.DEFAULT_DIALECT;
  }

  private String resolveSchema(String schema, List<String> tables) {
    if (StringUtils.hasText(schema)) {
      return schema.trim();
    }

    if (autoReadSchema) {
      return databaseSchemaService.loadSchemaDescription(tables);
    }

    if (StringUtils.hasText(defaultSchema)) {
      return defaultSchema.trim();
    }
    return Nl2SqlConstant.DEFAULT_SCHEMA_HINT;
  }

  private String resolveRules(String rules) {
    if (StringUtils.hasText(rules)) {
      return rules.trim();
    }
    if (StringUtils.hasText(defaultRules)) {
      return defaultRules.trim();
    }
    return Nl2SqlConstant.DEFAULT_RULES;
  }

  private String buildSystemPrompt(String dialect) {
    return """
        你是资深数据分析工程师，请将用户需求转换为可执行的 %s 查询 SQL。
        输出要求：
        1. 只输出 SQL，不要解释、不要 Markdown 代码块、不要前后缀。
        2. 只允许只读查询（SELECT/WITH/SHOW/DESC/EXPLAIN），禁止写操作和 DDL。
        3. 优先使用提供的表结构和字段，禁止臆造不存在的表或字段。
        4. 当需求有歧义时，生成最保守且可执行的查询。
        """.formatted(dialect);
  }

  private String buildUserPrompt(String question,
                                 String dialect,
                                 String schema,
                                 String rules,
                                 List<String> tables) {
    String tableRestriction = buildTableRestriction(tables);
    return """
        用户需求：
        %s

        SQL 方言：
        %s

        数据库结构：
        %s

        附加规则：
        %s

        可访问表限制：
        %s

        请仅返回一条 SQL。
        """.formatted(question, dialect, schema, rules, tableRestriction);
  }

  private String buildTableRestriction(List<String> tables) {
    if (tables == null || tables.isEmpty()) {
      return "未设置";
    }
    return String.join(", ", tables);
  }

}
