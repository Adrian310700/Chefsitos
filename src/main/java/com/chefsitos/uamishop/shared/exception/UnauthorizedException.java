package com.chefsitos.uamishop.shared.exception;

public class UnauthorizedException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String rule;

  public UnauthorizedException(String message) {
    super(message);
    this.rule = null;
  }

  public UnauthorizedException(String message, String rule) {
    super(message);
    this.rule = rule;
  }

  public String getRule() {
    return rule;
  }
}
