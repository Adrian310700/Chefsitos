package com.chefsitos.uamishop.shared.exception;

import lombok.Getter;

/**
 * Excepción lanzada cuando un servicio externo no está disponible
 * (circuit breaker abierto).
 */
@Getter
public class ServiceUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String serviceName;

    public ServiceUnavailableException(String message) {
        super(message);
        this.serviceName = null;
    }

    public ServiceUnavailableException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public ServiceUnavailableException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

}
