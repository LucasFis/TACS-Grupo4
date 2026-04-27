package app.servicios.impl;

import app.dto.CalificacionDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.NotificacionesDto;
import app.dto.OperacionesDto;
import app.dto.SugerenciaDto;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.Calificacion;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Sugerencia;
import app.model.entities.Perfil;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioPerfiles;
import app.servicios.IPerfilService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PerfilService implements IPerfilService {

    private final RepositorioPerfiles repositorioPerfiles;
    private final RepositorioPropuestas repositorioPropuestas;
    private final RepositorioSubastas repositorioSubastas;
    private final RepositorioFiguritasIntercambiables repositorioFiguritasIntercambiables;
    private final RepositorioNotificaciones repositorioNotificaciones;

    public PerfilService(RepositorioPerfiles repositorioPerfiles,
                         RepositorioPropuestas repositorioPropuestas,
                         RepositorioSubastas repositorioSubastas,
                         RepositorioNotificaciones repositorioNotificaciones,
                         RepositorioFiguritasIntercambiables
                                  repositorioFiguritasIntercambiables) {
        this.repositorioPerfiles = repositorioPerfiles;
        this.repositorioPropuestas = repositorioPropuestas;
        this.repositorioSubastas = repositorioSubastas;
        this.repositorioFiguritasIntercambiables = repositorioFiguritasIntercambiables;
        this.repositorioNotificaciones = repositorioNotificaciones;
    }

    @Override
    public OperacionesDto obtenerOperacionesPerfil(String userId) {
        Perfil usuario = repositorioPerfiles.buscarPorId(userId);
        if (usuario == null) {
            return null;
        }

        List<FiguritaIntercambiable> figuritasPublicadas = usuario.getColeccion().getRepetidas();

        List<Propuesta> enviadas = repositorioPropuestas.buscarPorAutorId(userId);
        List<Propuesta> recibidas = repositorioPropuestas.buscarPorDestinatarioId(userId);

        List<Subasta> subastasActivas = repositorioSubastas.buscarPorPerfilId(userId)
                .stream()
                .filter(Subasta::estaActivo)
                .toList();

        return new OperacionesDto(figuritasPublicadas, enviadas, recibidas, subastasActivas);
    }
//para cuando quiere realizar una propuesta
    @Override
    public List<FiguritaIntercambiableDto> obtenerIntercambiablesPerfil(String userId) {
        Perfil perfil = repositorioPerfiles.buscarPorId(userId);
        if (perfil == null) throw new NotFoundException("Perfil no encontrado");

        return repositorioFiguritasIntercambiables.buscarPorUsuarioId(userId)
            .stream()
            .map(FiguritaIntercambiableDto::new)
            .toList();
    }
    public CalificacionDto agregarCalificacion(String autorId, String perfilDestinoId, Integer valor, String descripcion) {
        if (valor == null) {
            throw new BadRequestException("El valor de la calificación no puede ser nulo");
        }
        if (valor < 1 || valor > 5) {
            throw new BadRequestException("El valor de la calificación debe estar entre 1 y 5");
        }

        Perfil perfilDestino = this.repositorioPerfiles.buscarPorId(perfilDestinoId);
        Perfil autor = this.repositorioPerfiles.buscarPorId(autorId);

        Calificacion calificacion = new Calificacion(
            UUID.randomUUID().toString(), //TODO ver
            autor,
            valor,
            descripcion
        );

        perfilDestino.getCalificaciones().add(calificacion);

        this.repositorioPerfiles.guardar(perfilDestino);
        return new CalificacionDto(calificacion, perfilDestino.obtenerCalificacionMedia());
    }

    @Override
    public List<SugerenciaDto> obtenerSugerencias(String userId) {
        Perfil perfilObjetivo = this.repositorioPerfiles.buscarPorId(userId);
        List<Perfil> perfiles = this.repositorioPerfiles.buscarTodos();
        List<Sugerencia> sugerencias = new ArrayList<>();

        perfiles.forEach(perfil -> {
            Sugerencia sugerencia = new Sugerencia(perfil, new ArrayList<>());

            perfil.getColeccion().getRepetidas().forEach(repetida -> {
                if(perfilObjetivo.getColeccion().getFaltantes().contains(repetida.getFigurita())){
                    sugerencia.getFiguritasSugeridas().add(repetida.getFigurita());
                }
            });

            if(!sugerencia.getFiguritasSugeridas().isEmpty()){
                sugerencias.add(sugerencia);
            }
        });

        return sugerencias.stream().map(SugerenciaDto::new).toList();
    }

    public List<NotificacionesDto> obtenerNotificaciones(String userId) {
        Perfil perfil = repositorioPerfiles.buscarPorId(userId);

        return this.repositorioNotificaciones.buscarPorUsuario(perfil).stream().map(NotificacionesDto::new).toList();
    }
}
