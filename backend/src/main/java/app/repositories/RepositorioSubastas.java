package app.repositories;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.Subasta;
import app.repositories.impl.campos.CamposSubasta;

import java.util.List;

public interface RepositorioSubastas {

    PaginaResultado<Subasta> buscarTodos(SubastasFiltro filtros, CamposSubasta campos);

    Subasta buscarPorId(String id, CamposSubasta campos);

    /**
     * Busca las subastas en las que un perfil ha participado realizando una oferta.
     *
     * @param userId identificador del perfil participante
     * @param campos configuración de campos a cargar en los resultados
     * @return lista de subastas en las que el perfil realizó al menos una oferta
     */
    List<Subasta> buscarDondeParticipa(String userId, CamposSubasta campos);

    int contar();

    void guardar(Subasta subasta);

    void guardar(Subasta subasta, CamposSubasta campos);

    PaginaResultado<Subasta> buscarPorAutor(String perfilId, Integer pagina, Integer limite, CamposSubasta campos);

    /**
     * Busca subastas activas que tengan como figurita subastada alguna de las indicadas.
     *
     * @param figuritaIds lista de identificadores de figuritas a buscar
     * @return lista de subastas activas que coinciden con las figuritas indicadas
     */
    List<Subasta> buscarActivasPorFiguritasSubastadas(List<String> figuritaIds);
}