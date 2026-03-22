package com.chefsitos.uamishop.shared.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String rule;

  public ConflictException(String message) {
    super(message);
    this.rule = null;
  }

  public ConflictException(String message, String rule) {
    super(message);
    this.rule = rule;
  }

}
