package app.repositories.impl;

import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Sugerencia;
import app.repositories.RepositorioSugerencias;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class RepositorioSugerenciasMongo implements RepositorioSugerencias {
  @Autowired
  MongoTemplate mongoTemplate;

  @Override
  public void guardar(Sugerencia sugerencia) {
    mongoTemplate.save(sugerencia);
  }

  @Override
  public void guardar(List<Sugerencia> sugerencias) {
    BulkOperations bulk = mongoTemplate.bulkOps(
        BulkOperations.BulkMode.UNORDERED,
        Sugerencia.class
    );

    for (Sugerencia n : sugerencias) {
      if (n.getId() == null) {
        bulk.insert(n); // ← MongoDB asigna el ID automáticamente
      } else {
        Query query = new Query(Criteria.where("_id").is(n.getId()));
        bulk.replaceOne(query, n, FindAndReplaceOptions.options().upsert());
      }
    }

    if (!sugerencias.isEmpty()) {
      bulk.execute();
    }
  }

  public Sugerencia buscarPorId(String id) {
    return mongoTemplate.findById(id, Sugerencia.class);
  }

  public void alternarFavorito(String id, String perfilId) {

    Query query = new Query(Criteria.where("_id").is(id).andOperator(Criteria.where("autor.$id").is(perfilId)));
    AggregationUpdate update = AggregationUpdate.update()
        .set("favorito").toValue(BooleanOperators.Not.not("$favorito"));
    UpdateResult result = mongoTemplate.updateFirst(query, update, Sugerencia.class);

    if (result.getMatchedCount() == 0) {
      throw new NotFoundException("Sugerencia no encontrada para el perfil utilizado");
    }
  }

  public List<Sugerencia> generarSugerencias(Perfil autor) {
    List<String> faltantesObjetivo = autor.getColeccion().getFaltantes()
        .stream().map(Figurita::getId).toList();

    List<AggregationOperation> ops = new ArrayList<>();

    ops.add(Aggregation.match(
        Criteria.where("_id").ne(autor.getColeccion().getId())
    ));

    ops.add(Aggregation.lookup("perfiles", "_id", "coleccion.$id", "perfil"));
    ops.add(Aggregation.unwind("perfil"));

    ops.add(context -> new Document("$lookup", new Document()
        .append("from", "sugerencias")
        .append("let", new Document("perfilId", "$perfil._id"))
        .append("pipeline", List.of(
            new Document("$match", new Document("$expr", new Document("$and", List.of(
                new Document("$eq", List.of("$autor.$id", autor.getId())),
                new Document("$eq", List.of("$sugerido.$id", "$$perfilId"))
            ))))
        ))
        .append("as", "sugerenciaExistente")
    ));

    ops.add(Aggregation.match(
        Criteria.where("sugerenciaExistente").is(List.of())
    ));

    List<String> repetidasObjetivoConIntercambio = autor.getColeccion().getRepetidas().stream()
        .filter(r -> r.getMetodos() != null && r.getMetodos().contains(MetodoIntercambio.INTERCAMBIO))
        .map(r -> r.getFigurita().getId())
        .toList();

    ops.add(context -> new Document("$addFields", new Document()
        .append("sugeridas", new Document("$filter", new Document()
            .append("input", "$repetidas")
            .append("as", "r")
            .append("cond", new Document("$and", List.of(
                new Document("$in", List.of(
                    new Document("$toString", "$$r.figurita.$id"),
                    faltantesObjetivo
                )),
                new Document("$in", List.of("INTERCAMBIO", "$$r.metodos")),
                new Document("$gt", List.of(
                    new Document("$subtract", List.of("$$r.cantidadExistente", "$$r.cantidadReservada")),
                    0
                ))
            )))
        ))
        .append("necesarias", new Document("$filter", new Document()
            .append("input", "$faltantes")
            .append("as", "f")
            .append("cond", new Document("$in", List.of(
                new Document("$toString", "$$f.$id"),
                repetidasObjetivoConIntercambio
            )))
        ))
    ));

    ops.add(Aggregation.match(
        new Criteria().andOperator(
            Criteria.where("sugeridas.0").exists(true),
            Criteria.where("necesarias.0").exists(true)
        )
    ));

    AggregationResults<Document> resultados = mongoTemplate.aggregate(
        Aggregation.newAggregation(ops), "colecciones", Document.class
    );

    MongoConverter converter = mongoTemplate.getConverter();
    List<Sugerencia> sugerencias = resultados.getMappedResults().stream()
        .map(doc -> {
          Perfil perfil = converter.read(Perfil.class, (Document) doc.get("perfil"));

          List<Figurita> sugeridas = ((List<Document>) doc.get("sugeridas")).stream()
              .map(d -> {
                Object figuritaRef = d.get("figurita");
                if (figuritaRef instanceof com.mongodb.DBRef ref) {
                  return mongoTemplate.findById(ref.getId().toString(), Figurita.class);
                }
                if (figuritaRef instanceof Document figDoc) {
                  return converter.read(Figurita.class, figDoc);
                }
                return null;
              })
              .filter(Objects::nonNull)
              .toList();

          List<Figurita> necesarias = ((List<Object>) doc.get("necesarias")).stream()
              .map(item -> {
                if (item instanceof com.mongodb.DBRef ref) {
                  return mongoTemplate.findById(ref.getId().toString(), Figurita.class);
                }
                return converter.read(Figurita.class, (Document) item);
              })
              .filter(Objects::nonNull)
              .toList();

          return Sugerencia.builder().autor(autor).sugerido(perfil).figuritasSugeridas(sugeridas).figuritasNecesarias(necesarias).build();
        })
        .toList();

    return sugerencias;
  }

  public PaginaResultado<Sugerencia> buscarPorPerfil(Perfil perfil, SugerenciasFiltro filtros) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("autor").is(perfil)
    );

    long count = mongoTemplate.count(query, Sugerencia.class);

    query.skip((long) (filtros.pagina()-1) * filtros.limite()).limit(filtros.limite());
    List<Sugerencia> sugerencias = mongoTemplate.find(query, Sugerencia.class);

    return new PaginaResultado<>(sugerencias, count, (int) Math.ceil((double) count / filtros.limite()), filtros.pagina());
  }

  public void eliminacionProgramada() {
    Query query = new Query();
    query.addCriteria(Criteria.where("favorito").is(false));

    mongoTemplate.findAllAndRemove(query, Sugerencia.class);
  }
}
