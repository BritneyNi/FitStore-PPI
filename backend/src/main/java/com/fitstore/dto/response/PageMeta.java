package com.fitstore.dto.response;

/**
 * Metadatos de paginación incluidos en {@link ApiResponse#paged}.
 *
 * @param page        página actual (1-based)
 * @param limit       tamaño de página
 * @param total       total de elementos disponibles
 * @param totalPages  total de páginas
 * @param hasNextPage hay una página siguiente
 * @param hasPrevPage hay una página anterior
 */
public record PageMeta(int page, int limit, long total, int totalPages, boolean hasNextPage, boolean hasPrevPage) {

    /** Meta para listas que se devuelven completas (sin paginar). */
    public static PageMeta all(long total) {
        return new PageMeta(1, (int) total, total, 1, false, false);
    }

    /** Meta para una página concreta. */
    public static PageMeta of(int page, int limit, long total) {
        int safeLimit = limit <= 0 ? (int) total : limit;
        int pages = safeLimit <= 0 ? 1 : (int) Math.ceil((double) total / safeLimit);
        return new PageMeta(page, safeLimit, total, pages, page < pages, page > 1);
    }
}
