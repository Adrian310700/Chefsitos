package com.chefsitos.uamishop.shared.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String rule;

  public BadRequestException(String message) {
    super(message);
    this.rule = null;
  }

  public BadRequestException(String message, String rule) {
    super(message);
    this.rule = rule;
  }

}
