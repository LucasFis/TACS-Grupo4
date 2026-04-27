package app.servicios;

import app.dto.CalificacionDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.NotificacionesDto;
import app.dto.OperacionesDto;
import app.dto.SugerenciaDto;
import app.model.entities.Calificacion;

import java.util.List;

public interface IPerfilService {
    OperacionesDto obtenerOperacionesPerfil(String userId);

    List<FiguritaIntercambiableDto> obtenerIntercambiablesPerfil(String userId);
    CalificacionDto agregarCalificacion(String autorId, String perfilDestinoId, Integer valor,
                                        String descripcion);
    List<SugerenciaDto> obtenerSugerencias(String userId);
    List<NotificacionesDto> obtenerNotificaciones(String userId);
}
