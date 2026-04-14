package app.servicios.impl;

import app.dto.FiguritaIntercambiableDto;
import app.dto.NotificacionesDto;
import app.dto.OperacionesDto;
import app.dto.SugerenciaDto;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Sugerencia;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import app.servicios.UsuarioService;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioPropuestas repositorioPropuestas;
    private final RepositorioSubastas repositorioSubastas;
    private final RepositorioFiguritasIntercambiables repositorioFiguritasIntercambiables;
    private final RepositorioNotificaciones repositorioNotificaciones;

    public UsuarioServiceImpl(RepositorioUsuarios repositorioUsuarios,
                              RepositorioPropuestas repositorioPropuestas,
                              RepositorioSubastas repositorioSubastas,
                              RepositorioNotificaciones repositorioNotificaciones,
                              RepositorioFiguritasIntercambiables
                                  repositorioFiguritasIntercambiables) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioPropuestas = repositorioPropuestas;
        this.repositorioSubastas = repositorioSubastas;
        this.repositorioFiguritasIntercambiables = repositorioFiguritasIntercambiables;
        this.repositorioNotificaciones = repositorioNotificaciones;
    }

    @Override
    public OperacionesDto getOperacionesUsuario(String userId) {
        Usuario usuario = repositorioUsuarios.findById(userId);
        if (usuario == null) {
            return null;
        }

        List<FiguritaIntercambiable> figuritasPublicadas = usuario.getColeccion().getRepetidas();

        List<Propuesta> enviadas = repositorioPropuestas.findByOrigenId(userId);
        List<Propuesta> recibidas = repositorioPropuestas.findByDestinoId(userId);

        List<Subasta> subastasActivas = repositorioSubastas.findByUsuarioId(userId)
                .stream()
                .filter(Subasta::estaActivo)
                .toList();

        return new OperacionesDto(figuritasPublicadas, enviadas, recibidas, subastasActivas);
    }
//para cuando quiere realizar una propuesta
    @Override
    public List<FiguritaIntercambiableDto> getIntercambiablesUsuario(String userId) {
        Usuario usuario = repositorioUsuarios.findById(userId);
        if (usuario == null) throw new NotFoundException("Usuario no encontrado");

        return repositorioFiguritasIntercambiables.buscarPorUsuarioId(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }
//implementar mappers en lugar de tener la logica aca
    private FiguritaIntercambiableDto toDto(FiguritaIntercambiable fi) {
        return new FiguritaIntercambiableDto(
            fi.getFigurita().getId(),
            fi.getFigurita().getNumero(),
            fi.getFigurita().getJugador(),
            fi.getFigurita().getSeleccion(),
            fi.getCantidadDisponible(),
            fi.getMetodos(),
            fi.getUsuarioId()
        );
    }
    public Number agregarCalificacion(Integer calificacion, String userId) {
        Usuario usuario = this.repositorioUsuarios.findById(userId);

        if(calificacion == null) {
            throw new BadRequestException("La calificacion no puede ser nula");
        }
        if(calificacion < 0 || calificacion > 10) {
            throw new BadRequestException("La calificacion debe estar entre 0 y 10");
        }

        usuario.getCalificaciones().add(calificacion);

        this.repositorioUsuarios.save(usuario);
        return usuario.getCalificacionMedia();
    }

    @Override
    public List<SugerenciaDto> getSugerencias(String userId) {
        Usuario usuarioObjetivo = this.repositorioUsuarios.findById(userId);
        List<Usuario> usuarios = this.repositorioUsuarios.findAll();
        List<Sugerencia> sugerencias = new ArrayList<>();

        usuarios.forEach(usuario -> {
            Sugerencia sugerencia = new Sugerencia(usuario, new ArrayList<>());

            usuario.getColeccion().getRepetidas().forEach(repetida -> {
                if(usuarioObjetivo.getColeccion().getFaltantes().contains(repetida.getFigurita())){
                    sugerencia.getFiguritasSugeridas().add(repetida.getFigurita());
                }
            });

            if(!sugerencia.getFiguritasSugeridas().isEmpty()){
                sugerencias.add(sugerencia);
            }
        });

        return sugerencias.stream().map(SugerenciaDto::new).toList();
    }

    public List<NotificacionesDto> getNotificaciones(String userId) {
        Usuario usuario = repositorioUsuarios.findById(userId);

        return this.repositorioNotificaciones.buscarPorUsuario(usuario).stream().map(NotificacionesDto::new).toList();
    }
}
