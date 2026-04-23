package app.servicios.impl;

import app.dto.EstadisticasDto;
import app.model.entities.Subasta;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import app.servicios.IEstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements IEstadisticasService {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioPropuestas repositorioPropuestas;
    private final RepositorioSubastas repositorioSubastas;

    @Override
    public EstadisticasDto obtenerEstadisticas() {
        int totalUsuarios = repositorioUsuarios.contar();

        int totalFiguritasPublicadas = repositorioUsuarios.buscarTodos().stream()
                .mapToInt(u -> u.getColeccion().getRepetidas().size())
                .sum();

        int totalPropuestas = repositorioPropuestas.contar();

        int totalSubastasActivas = (int) repositorioSubastas.buscarTodos().stream()
                .filter(Subasta::estaActivo)
                .count();

        return new EstadisticasDto(totalUsuarios, totalFiguritasPublicadas,
                totalPropuestas, totalSubastasActivas);
    }
}
