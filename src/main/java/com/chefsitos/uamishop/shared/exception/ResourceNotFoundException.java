package com.chefsitos.uamishop.shared.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no es encontrado.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String resourceName;
    private final String resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.resourceId = null;
    }

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + " no encontrado con ID: " + resourceId);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceId() {
        return resourceId;
    }
}
