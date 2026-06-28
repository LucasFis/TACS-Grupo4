package app.repositories.impl;

import app.dto.filtros.FiguritasFiltro;
import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.repositories.RepositorioFiguritas;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioFiguritasMongo implements RepositorioFiguritas {

  private static final String STATUS_EN_PROCESO = "EN_PROCESO";
  private static final String STATUS_COMPLETADO = "COMPLETADO";

  @Autowired
  MongoTemplate mongoTemplate;

  @Override
  public Figurita buscarPorId(String id) {
    Figurita figurita = this.mongoTemplate.findById(id, Figurita.class);

    if (figurita == null) {
      throw new NotFoundException("Figurita no encontrada");
    }

    return figurita;
  }

  @Override
  public List<Figurita> buscarPorIds(List<String> ids) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("_id").in(ids)
    );
    return this.mongoTemplate.find(query, Figurita.class);
  }

  @Override
  public List<Figurita> buscarConFiltros(FiguritasFiltro filtros) {
    Query query = new Query();
    if (filtros.id() != null) {
      query.addCriteria(Criteria.where("_id").is(filtros.id()));
    }
    if (filtros.jugador() != null) {
      query.addCriteria(Criteria.where("jugador").regex(filtros.jugador(), "i"));
    }
    if (filtros.numero() != null) {
      query.addCriteria(Criteria.where("numero").is(filtros.numero()));
    }
    if (filtros.seleccion() != null) {
      query.addCriteria(Criteria.where("seleccion").regex(filtros.seleccion(), "i"));
    }

    int tamanioPagina = filtros.tamanioPaginaEfectivo();
    query.skip((long) filtros.paginaEfectiva() * tamanioPagina).limit(tamanioPagina);

    return this.mongoTemplate.find(query, Figurita.class);
  }

  @Override
  public void guardar(Figurita figurita) {
    this.mongoTemplate.save(figurita);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Utiliza un criteria que considera pendientes aquellas sin {@code imagenStatus}
   * o con estado {@code EN_PROCESO} cuyo {@code imagenCreadoEn} haya expirado
   * según el TTL provisto.
   * </p>
   */
  @Override
  public List<Figurita> buscarPendientes(Duration ttlProcesamiento, Duration ttlRefresco, int tamanioPagina) {
    Query query = new Query(pendientesCriteria(ttlProcesamiento, ttlRefresco)).limit(tamanioPagina);
    return mongoTemplate.find(query, Figurita.class);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Implementación atómica con {@code findAndModify}: actualiza el documento
   * a {@code EN_PROCESO} solo si aún está pendiente, evitando que dos
   * instancias procesen la misma figurita simultáneamente.
   * </p>
   */
  @Override
  public Figurita reclamarParaProcesamiento(String figuritaId, Duration ttlProcesamiento, Duration ttlRefresco) {
    Query query = new Query(
        new Criteria().andOperator(
            Criteria.where("_id").is(figuritaId),
            pendientesCriteria(ttlProcesamiento, ttlRefresco)
        )
    );
    Update update = new Update()
        .set("imagenStatus", STATUS_EN_PROCESO)
        .set("imagenCreadoEn", LocalDateTime.now());

    return mongoTemplate.findAndModify(query, update,
        FindAndModifyOptions.options().returnNew(false), Figurita.class);
  }

  private static Criteria pendientesCriteria(Duration ttlProcesamiento, Duration ttlRefresco) {
    LocalDateTime procesamientoExpiradoAntes = LocalDateTime.now().minus(ttlProcesamiento);
    LocalDateTime refrescoExpiradoAntes = LocalDateTime.now().minus(ttlRefresco);
    return new Criteria().orOperator(
        Criteria.where("imagenStatus").is(null),
        Criteria.where("imagenStatus").is(STATUS_EN_PROCESO)
                .and("imagenCreadoEn").lt(procesamientoExpiradoAntes),
        Criteria.where("imagenStatus").is(STATUS_COMPLETADO)
                .and("imagenCreadoEn").lt(refrescoExpiradoAntes)
    );
  }
}
