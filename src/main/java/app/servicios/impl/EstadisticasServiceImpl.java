package app.servicios.impl;

import app.dto.EstadisticasDto;
import app.model.entities.Subasta;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import app.servicios.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements EstadisticasService {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioPropuestas repositorioPropuestas;
    private final RepositorioSubastas repositorioSubastas;

    @Override
    public EstadisticasDto getEstadisticas() {
        int totalUsuarios = repositorioUsuarios.count();

        int totalFiguritasPublicadas = repositorioUsuarios.findAll().stream()
                .mapToInt(u -> u.getColeccion().getRepetidas().size())
                .sum();

        int totalPropuestas = repositorioPropuestas.count();

        int totalSubastasActivas = (int) repositorioSubastas.findAll().stream()
                .filter(Subasta::estaActivo)
                .count();

        return new EstadisticasDto(totalUsuarios, totalFiguritasPublicadas,
                totalPropuestas, totalSubastasActivas);
    }
}
