package com.chefsitos.uamishop.shared.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String rule;

  public ForbiddenException(String message) {
    super(message);
    this.rule = null;
  }

  public ForbiddenException(String message, String rule) {
    super(message);
    this.rule = rule;
  }

}
