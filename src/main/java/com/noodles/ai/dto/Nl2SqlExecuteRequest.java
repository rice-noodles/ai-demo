package com.noodles.ai.dto;

import java.util.List;

/**
 * @author Noodles
 * @date 2026/3/26 12:03
 */
public record Nl2SqlExecuteRequest(
    String question,
    String schema,
    String dialect,
    String additionalRules,
    List<String> tables,
    String resultType,
    Integer maxRows
) {
}
