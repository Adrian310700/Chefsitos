package com.chefsitos.uamishop.shared.exception;

public class BusinessRuleException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String rule;

  public BusinessRuleException(String message) {
    super(message);
    this.rule = null;
  }

  public BusinessRuleException(String message, String rule) {
    super(message);
    this.rule = rule;
  }

  public String getRule(){
    return rule;
  }
}
