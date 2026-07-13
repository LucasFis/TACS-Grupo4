package app.dto.filtros;

public record NotificacionesFiltro(
    Boolean leida,
    Integer pagina,
    Integer limite
) {
    public NotificacionesFiltro {
        if (pagina == null) pagina = 1;
        if (limite == null) limite = 10;
    }
}
