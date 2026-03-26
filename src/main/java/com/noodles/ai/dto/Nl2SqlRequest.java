package com.noodles.ai.dto;

import java.util.List;

/**
 * @author Noodles
 * @date 2026/3/26 11:16
 */
public record Nl2SqlRequest(
    String question,
    String schema,
    String dialect,
    String additionalRules,
    List<String> tables
) {
}
