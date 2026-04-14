package app.servicios;

import app.dto.OperacionesDto;
import app.model.entities.Sugerencia;

import java.util.List;

public interface UsuarioService {
    OperacionesDto getOperacionesUsuario(String userId);
    Number agregarCalificacion(Integer calificacion, String userId);
    List<Sugerencia> getSugerencias(String userId);
}
