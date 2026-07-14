package app.dto;

import java.util.List;

public record NotificacionesPaginadasDto(
    List<NotificacionDto> contenido,
    long cantidadDeElementos,
    int cantidadDePaginas,
    int numero,
    long noLeidas
) {}
