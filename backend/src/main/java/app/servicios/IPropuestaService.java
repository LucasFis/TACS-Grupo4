package app.servicios;

import app.dto.PropuestaDto;
import app.dto.request.CrearPropuestaRequest;
import app.model.entities.Propuesta;

public interface IPropuestaService {
    PropuestaDto crearPropuesta(CrearPropuestaRequest request);
    void aceptar(String id, String usuarioId);
    void rechazar(String id, String usuarioId);
}
