package com.fitstore.dto.response;

import java.util.List;

/**
 * Utilidad para envolver listas en {@link ApiResponse} con soporte de paginación
 * opcional mediante los parámetros {@code page} y {@code limit}.
 *
 * <p>Si no se envían dichos parámetros, se devuelve la lista completa
 * (comportamiento por defecto del catálogo y de los listados).
 */
public final class Paginacion {

    private Paginacion() {}

    /**
     * @param all   lista completa
     * @param page  página solicitada (1-based) o {@code null} para devolver todo
     * @param limit tamaño de página o {@code null} para devolver todo
     */
    public static <T> ApiResponse<List<T>> de(List<T> all, Integer page, Integer limit) {
        if (page == null || limit == null) {
            return ApiResponse.paged(all, PageMeta.all(all.size()));
        }
        int p = Math.max(1, page);
        int l = Math.max(1, limit);
        int from = Math.min((p - 1) * l, all.size());
        int to = Math.min(from + l, all.size());
        return ApiResponse.paged(all.subList(from, to), PageMeta.of(p, l, all.size()));
    }
}
