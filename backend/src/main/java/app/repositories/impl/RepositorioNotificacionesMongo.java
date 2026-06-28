package app.repositories.impl;

import app.model.entities.Perfil;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioNotificaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RepositorioNotificacionesMongo implements RepositorioNotificaciones {

  @Autowired
  MongoTemplate mongoTemplate;

  @Override
  public void guardar(Notificacion notificacion) {
    this.mongoTemplate.save(notificacion);
  }

  @Override
  public void guardar(List<Notificacion> notificaciones) {
    BulkOperations bulk = mongoTemplate.bulkOps(
        BulkOperations.BulkMode.UNORDERED,
        Notificacion.class
    );

    for (Notificacion n : notificaciones) {

      Query query = new Query(
          Criteria.where("_id").is(n.getId())
      );

      bulk.replaceOne(
          query,
          n,
          FindAndReplaceOptions.options().upsert()
      );
    }

    bulk.execute();
  }

    @Override
    public List<Notificacion> buscarPorPerfil(Perfil perfil) {
      Query query = new Query();
      query.addCriteria(
          Criteria.where("perfil").is(perfil.getId())
      );

      return this.mongoTemplate.find(query, Notificacion.class);
    }

  @Override
  public List<Notificacion> buscarPorPerfilFechaDesc(String perfilId) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("perfil").is(perfilId)
    );

    query.with(Sort.by(Sort.Direction.DESC, "mensaje.fecha"));

    return this.mongoTemplate.find(query, Notificacion.class);
  }
}
