package app.servicios;

import app.dto.FiguritaIntercambiableDto;
import app.dto.NotificacionesDto;
import app.dto.OperacionesDto;
import app.dto.SugerenciaDto;
import app.model.entities.Sugerencia;
import app.model.notificador.Notificacion;
import java.util.List;

public interface IUsuarioService {
    OperacionesDto obtenerOperacionesUsuario(String userId);

    List<FiguritaIntercambiableDto> obtenerIntercambiablesUsuario(String userId);
    Number agregarCalificacion(Integer calificacion, String userId);
    List<SugerenciaDto> obtenerSugerencias(String userId);
    List<NotificacionesDto> obtenerNotificaciones(String userId);
}
