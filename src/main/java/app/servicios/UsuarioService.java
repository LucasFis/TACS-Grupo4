package app.servicios;

import app.dto.FiguritaIntercambiableDto;
import app.dto.NotificacionesDto;
import app.dto.OperacionesDto;
import app.dto.SugerenciaDto;
import app.model.entities.Sugerencia;
import app.model.notificador.Notificacion;
import java.util.List;

public interface UsuarioService {
    OperacionesDto getOperacionesUsuario(String userId);

    List<FiguritaIntercambiableDto> getIntercambiablesUsuario(String userId);
    Number agregarCalificacion(Integer calificacion, String userId);
    List<SugerenciaDto> getSugerencias(String userId);
    List<NotificacionesDto> getNotificaciones(String userId);
}
