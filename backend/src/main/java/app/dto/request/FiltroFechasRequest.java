package app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FiltroFechasRequest(
    @NotBlank(message = "el parámetro 'desde' es requerido") String desde,
    @NotBlank(message = "el parámetro 'hasta' es requerido") String hasta
) {
}
