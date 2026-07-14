package app.repositories.impl;

import app.dto.paginacion.PaginaResultado;
import app.model.entities.Calificacion;
import app.model.entities.MetodoIntercambio;
import app.repositories.RepositorioCalificacion;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.bson.types.ObjectId;
import java.util.Arrays;

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
              criterioDbRefId("destinatario.$id", destinatarioId)
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
              criterioDbRefId("destinatario.$id", perfilDestinoId)
      );

      query.addCriteria(
              criterioDbRefId("autor.$id", perfilAutorId)
      );

    query.addCriteria(
        Criteria.where("transaccionId").is(transaccionId)
    );

    query.addCriteria(
        Criteria.where("tipoTransaccion").is(tipoTransaccion.name())
    );

    return mongoTemplate.exists(query, Calificacion.class);
  }

  @Override
  public Set<String> obtenerTransaccionesCalificadas(String autorId, MetodoIntercambio tipo, Set<String> transaccionIds) {
    if (transaccionIds.isEmpty()) return Set.of();

    Query query = new Query();
    query.addCriteria(Criteria.where("autor.$id").is(autorId));
    query.addCriteria(Criteria.where("tipoTransaccion").is(tipo.name()));
    query.addCriteria(Criteria.where("transaccionId").in(transaccionIds));
    query.fields().include("transaccionId");

    return mongoTemplate.find(query, Calificacion.class).stream()
        .map(Calificacion::getTransaccionId)
        .collect(Collectors.toSet());
  }

  private Criteria criterioDbRefId(String campo, String id) {
    try {
      return Criteria.where(campo).in(Arrays.asList(id, new ObjectId(id)));
    } catch (IllegalArgumentException e) {
      return Criteria.where(campo).is(id);
    }
  }
}
