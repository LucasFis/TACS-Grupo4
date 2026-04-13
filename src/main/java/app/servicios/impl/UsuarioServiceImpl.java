package app.servicios.impl;

import app.dto.OperacionesDto;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import app.servicios.UsuarioService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioPropuestas repositorioPropuestas;
    private final RepositorioSubastas repositorioSubastas;

    public UsuarioServiceImpl(RepositorioUsuarios repositorioUsuarios,
                              RepositorioPropuestas repositorioPropuestas,
                              RepositorioSubastas repositorioSubastas) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioPropuestas = repositorioPropuestas;
        this.repositorioSubastas = repositorioSubastas;
    }

    @Override
    public OperacionesDto getOperacionesUsuario(String userId) {
        Usuario usuario = repositorioUsuarios.findById(userId);
        if (usuario == null) {
            return null;
        }

        List<FiguritaIntercambiable> figuritasPublicadas = usuario.getColeccion().getRepetidas();

        List<Propuesta> enviadas  = repositorioPropuestas.findByOrigenId(userId);
        List<Propuesta> recibidas = repositorioPropuestas.findByDestinoId(userId);

        List<Subasta> subastasActivas = repositorioSubastas.findByUsuarioId(userId)
                .stream()
                .filter(Subasta::estaActivo)
                .toList();

        return new OperacionesDto(figuritasPublicadas, enviadas, recibidas, subastasActivas);
    }
}
