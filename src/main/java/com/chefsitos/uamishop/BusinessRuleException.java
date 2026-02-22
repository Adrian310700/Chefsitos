package com.chefsitos.uamishop;

/**
 * Excepción lanzada cuando se viola una regla de negocio.
 */
public class BusinessRuleException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String rule;

  public BusinessRuleException(String message) {
    super(message);
    this.rule = null;
  }

  public BusinessRuleException(String rule, String message) {
    super(message);
    this.rule = rule;
  }

  public String getRule() {
    return rule;
  }
}
