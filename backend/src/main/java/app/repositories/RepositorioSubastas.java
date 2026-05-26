package app.repositories;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.Subasta;
import app.repositories.impl.campos.CamposSubasta;

import java.util.List;

public interface RepositorioSubastas {

    PaginaResultado<Subasta> buscarTodos(SubastasFiltro filtros, CamposSubasta campos);

    Subasta buscarPorId(String id, CamposSubasta campos);

    List<Subasta> buscarDondeParticipa(String userId, CamposSubasta campos);

    int contar();

    void guardar(Subasta subasta);

    void guardar(Subasta subasta, CamposSubasta campos);

    PaginaResultado<Subasta> buscarPorAutor(String perfilId, Integer pagina, Integer limite, CamposSubasta campos);
}