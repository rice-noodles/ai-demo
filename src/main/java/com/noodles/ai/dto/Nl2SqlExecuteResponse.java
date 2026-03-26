package com.noodles.ai.dto;

import java.util.List;

/**
 * @author Noodles
 * @date 2026/3/26 12:03
 */
public record Nl2SqlExecuteResponse(
    String sql,
    String model,
    String resultType,
    int rowCount,
    List<?> rows
) {
}
