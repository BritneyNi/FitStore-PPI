package com.fitstore.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Respuesta estándar de la API.
 *
 * <pre>{ "data": <contenido>, "meta": { ... } }</pre>
 *
 * {@code meta} solo se incluye en listas paginadas.
 *
 * @param <T> tipo del contenido devuelto
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final T data;
    private final PageMeta meta;

    private ApiResponse(T data, PageMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    /** Respuesta de un recurso único o un objeto simple. */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }

    /** Respuesta de una lista, opcionalmente paginada. */
    public static <T> ApiResponse<T> paged(T data, PageMeta meta) {
        return new ApiResponse<>(data, meta);
    }

    public T getData() {
        return data;
    }

    public PageMeta getMeta() {
        return meta;
    }
}
