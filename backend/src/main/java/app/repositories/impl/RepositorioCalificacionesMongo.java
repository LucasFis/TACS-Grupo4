package app.repositories.impl;

import app.dto.paginacion.PaginaResultado;
import app.model.entities.Calificacion;
import app.model.entities.MetodoIntercambio;
import app.repositories.RepositorioCalificacion;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCalificacionesMongo implements RepositorioCalificacion {
  @Autowired
  private MongoTemplate mongoTemplate;

  public void guardar(Calificacion calificacion) {
    mongoTemplate.save(calificacion);
  }

  public PaginaResultado<Calificacion> buscarPorDestinatario(
      String destinatarioId,
      Integer pagina,
      Integer limite
  ) {

    Query query = new Query();

    query.addCriteria(
        Criteria.where("destinatario.$id").is(destinatarioId)
    );

    long count = mongoTemplate.count(query, Calificacion.class);

    query.skip((long) (pagina - 1) * limite);
    query.limit(limite);

    List<Calificacion> contenido =
        mongoTemplate.find(query, Calificacion.class);

    return new PaginaResultado<>(
        contenido,
        count,
        (int) Math.ceil((double) count / limite),
        pagina
    );
  }

  public boolean yaCalifico(
      String perfilDestinoId,
      String perfilAutorId,
      String transaccionId,
      MetodoIntercambio tipoTransaccion
  ) {

    Query query = new Query();

    query.addCriteria(
        Criteria.where("destinatario.$id").is(perfilDestinoId)
    );

    query.addCriteria(
        Criteria.where("autor.$id").is(perfilAutorId)
    );

    query.addCriteria(
        Criteria.where("transaccionId").is(transaccionId)
    );

    query.addCriteria(
        Criteria.where("tipoTransaccion").is(tipoTransaccion.name())
    );

    return mongoTemplate.exists(query, Calificacion.class);
  }
}
