package com.noodles.ai.controller;

import com.noodles.ai.dto.Nl2SqlExecuteRequest;
import com.noodles.ai.dto.Nl2SqlExecuteResponse;
import com.noodles.ai.service.Nl2SqlQueryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Noodles
 * @date 2026/3/26 11:21
 */
@AllArgsConstructor
@RestController
@RequestMapping("/nl2sql")
public class Nl2SqlController {

  private final Nl2SqlQueryService nl2SqlQueryService;

  @PostMapping("/execute")
  public Nl2SqlExecuteResponse generateAndExecute(@RequestBody Nl2SqlExecuteRequest request) {
    if (request == null || !StringUtils.hasText(request.question())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question 不能为空");
    }

    try {
      return nl2SqlQueryService.generateAndExecute(request);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex);
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "SQL 执行失败: " + ex.getMessage(), ex);
    }
  }

}
