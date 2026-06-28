package app.repositories;

import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.Perfil;
import app.model.entities.Sugerencia;
import app.repositories.impl.campos.CamposPerfil;

import java.util.List;

public interface RepositorioPerfiles {

    /**
     * Persiste un perfil completo.
     *
     * @param perfil el perfil a guardar
     */
    void guardar(Perfil perfil);

    /**
     * Actualiza selectivamente los campos de un perfil según los flags
     * de {@link CamposPerfil}.
     *
     * @param perfil perfil con los datos a persistir
     * @param campos especifica si incluir medios de contacto
     */
    void guardar(Perfil perfil, CamposPerfil campos);

    /**
     * Busca un perfil por su identificador, cargando selectivamente los campos indicados.
     *
     * @param id     identificador del perfil
     * @param campos especifica qué campos incluir
     * @return el perfil encontrado
     * @throws app.exceptions.NotFoundException si no existe el perfil
     */
    Perfil buscarPorId(String id, CamposPerfil campos);

    /**
     * Busca un perfil por el identificador del usuario asociado.
     *
     * @param usuarioId identificador del usuario
     * @param campos    especifica qué campos incluir
     * @return el perfil encontrado
     * @throws app.exceptions.NotFoundException si no existe un perfil para ese usuario
     */
    Perfil buscarPorUsuarioId(String usuarioId, CamposPerfil campos);

    /**
     * Obtiene todos los perfiles del sistema, con carga selectiva de campos.
     *
     * @param campos especifica qué campos incluir
     * @return lista de todos los perfiles
     */
    List<Perfil> buscarTodos(CamposPerfil campos);

    /**
     * Cuenta la cantidad total de perfiles registrados.
     *
     * @return cantidad de perfiles
     */
    long contar();

    /**
     * Busca todos los perfiles que tienen una figurita específica como faltante.
     *
     * @param figurita figurita por la cual filtrar
     * @param campos   especifica qué campos incluir
     * @return lista de perfiles que tienen la figurita como faltante
     */
    List<Perfil> buscarPorFiguritaFaltante(Figurita figurita, CamposPerfil campos);

    /**
     * Genera sugerencias de intercambio para una colección objetivo, cruzando faltantes
     * y repetidos con otras colecciones. Soporta filtros de tipo de sugerencia
     * ({@code 1a1}, {@code Na1}, {@code 1aN}).
     *
     * @param coleccion colección para la cual generar sugerencias
     * @param filtros   criterios de filtrado (tipo, paginación)
     * @return página de sugerencias de intercambio
     */
    PaginaResultado<Sugerencia> generarSugerencias(Coleccion coleccion, SugerenciasFiltro filtros);
}
