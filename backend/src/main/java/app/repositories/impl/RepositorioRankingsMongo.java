package app.repositories.impl;

import app.dto.RankingUsuarioDto;
import app.repositories.RepositorioRankings;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Repository
@AllArgsConstructor
public class RepositorioRankingsMongo implements RepositorioRankings {

  private MongoTemplate mongoTemplate;

  @Override
  public List<RankingUsuarioDto> topCreadoresDePropuestas(LocalDateTime desde, LocalDateTime hasta, int limite) {
    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.match(Criteria.where("estado.0.fecha").gte(desde).lte(hasta)));
    agruparContarOrdenarYLookup(ops, "autor.$id", limite);
    return ejecutarConsulta(ops, "propuestas");
  }

  @Override
  public List<RankingUsuarioDto> topSubastadores(LocalDateTime desde, LocalDateTime hasta, int limite) {
    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.match(Criteria.where("fechaInicio").gte(desde).lte(hasta)));
    agruparContarOrdenarYLookup(ops, "autor.$id", limite);
    return ejecutarConsulta(ops, "subastas");
  }

  @Override
  public List<RankingUsuarioDto> topIntercambiadores(LocalDateTime desde, LocalDateTime hasta, int limite) {
    Document ultimoEstado = new Document("$arrayElemAt", List.of("$estado.valor", -1));
    Document filtrarAceptadas = new Document("$match",
        new Document("$expr", new Document("$eq", List.of(ultimoEstado, "ACEPTADO"))));
    Document participantesDeAmbos = new Document("$concatArrays",
        List.of(List.of("$autor.$id"), List.of("$destinatario.$id")));

    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.match(Criteria.where("estado.0.fecha").gte(desde).lte(hasta)));
    ops.add(context -> filtrarAceptadas);
    ops.add(AddFieldsOperation.addField("participantes").withValue(participantesDeAmbos).build());
    ops.add(Aggregation.unwind("participantes"));
    agruparContarOrdenarYLookup(ops, "participantes", limite);

    return ejecutarConsulta(ops, "propuestas");
  }

  @Override
  public List<RankingUsuarioDto> mejorTasaAceptacion(LocalDateTime desde, LocalDateTime hasta, int minimo, int limite) {
    Document ultimoEstado = new Document("$arrayElemAt", List.of("$estado.valor", -1));
    Document esAceptada = new Document("$cond", List.of(
        new Document("$eq", List.of(ultimoEstado, "ACEPTADO")), 1, 0));
    Document agruparPorDestinatario = new Document("$group", new Document("_id", "$destinatario.$id")
        .append("total", new Document("$sum", 1))
        .append("aceptadas", new Document("$sum", esAceptada)));
    Document porcentaje = new Document("$multiply", List.of(
        new Document("$divide", List.of("$aceptadas", "$total")), 100));
    Document detalleFraccion = new Document("$concat",
        List.of(new Document("$toString", "$aceptadas"), "/", new Document("$toString", "$total")));

    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.match(Criteria.where("estado.0.fecha").gte(desde).lte(hasta)));
    ops.add(context -> agruparPorDestinatario);
    ops.add(Aggregation.match(Criteria.where("total").gte(minimo)));
    ops.add(AddFieldsOperation.addField("valor").withValue(porcentaje).build());
    ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "valor")));
    ops.add(Aggregation.limit(limite));
    ops.addAll(lookupPerfilYProyectar(detalleFraccion));

    return ejecutarConsulta(ops, "propuestas");
  }

  @Override
  public List<RankingUsuarioDto> mejorReputacion(int minimoCalificaciones, int limite) {
    Document cantidadComoTexto = new Document("$toString", "$cantidadCalificaciones");
    Document detalleCalificaciones = new Document("$concat", List.of(cantidadComoTexto, " calificaciones"));

    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.match(Criteria.where("cantidadCalificaciones").gte(minimoCalificaciones)));
    ops.add(Aggregation.sort(
            Sort.by(
                Sort.Order.desc("calificacionMedia"),
                Sort.Order.desc("cantidadCalificaciones"))
        )
    );

    ops.add(Aggregation.limit(limite));
    ops.add(Aggregation.project()
        .andExclude("_id")
        .and(ctx -> new Document("$toString", "$_id")).as("perfilId")
        .and("nombre").as("nombre")
        .and("calificacionMedia").as("valor")
        .and(ctx -> detalleCalificaciones).as("detalle"));

    return ejecutarConsulta(ops, "perfiles");
  }

  @Override
  public List<RankingUsuarioDto> topColeccionistas(int limite) {
    Document totalRepetidas = new Document("$sum", "$col.repetidas.cantidadExistente");

    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.lookup("colecciones", "coleccion.$id", "_id", "col"));
    ops.add(Aggregation.unwind("col"));
    ops.add(AddFieldsOperation.addField("valor").withValue(totalRepetidas).build());
    ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "valor")));
    ops.add(Aggregation.limit(limite));
    ops.add(Aggregation.project()
        .andExclude("_id")
        .and(ctx -> new Document("$toString", "$_id")).as("perfilId")
        .and("nombre").as("nombre")
        .and("valor").as("valor"));

    return ejecutarConsulta(ops, "perfiles");
  }

  private void agruparContarOrdenarYLookup(List<AggregationOperation> ops, String campo, int limite) {
    ops.add(Aggregation.group(campo).count().as("valor"));
    ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "valor")));
    ops.add(Aggregation.limit(limite));
    ops.addAll(lookupPerfilYProyectar(null));
  }

  /**
   * Etapas finales comunes: resuelve el nombre del perfil con un {@code $lookup}
   * y proyecta al formato de {@link RankingUsuarioDto}.
   *
   * @param detalle expresión opcional para el campo detalle (puede ser {@code null})
   */
  private List<AggregationOperation> lookupPerfilYProyectar(Document detalle) {
    ProjectionOperation proyectar = Aggregation.project()
        .andExclude("_id")
        .and(ctx -> new Document("$toString", "$_id")).as("perfilId")
        .and("perfil.nombre").as("nombre")
        .and("valor").as("valor");

    if (detalle != null) {
      proyectar = proyectar.and(ctx -> detalle).as("detalle");
    }

    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.lookup("perfiles", "_id", "_id", "perfil"));
    ops.add(Aggregation.unwind("perfil"));
    ops.add(proyectar);

    return ops;
  }

  /**
   * Ejecuta el pipeline de agregación sobre la colección indicada y mapea
   * los documentos resultantes a {@link RankingUsuarioDto}.
   */
  private List<RankingUsuarioDto> ejecutarConsulta(List<AggregationOperation> ops, String coleccion) {
    AggregationResults<Document> resultado = mongoTemplate
        .aggregate(Aggregation.newAggregation(ops), coleccion, Document.class);

    return resultado.getMappedResults().stream().map(this::mapear).toList();
  }

  private RankingUsuarioDto mapear(Document doc) {
    Object perfilIdRaw = doc.get("perfilId");
    String perfilId = perfilIdRaw != null ? perfilIdRaw.toString() : null;

    Number valorRaw = doc.get("valor", Number.class);
    double valor = valorRaw != null ? valorRaw.doubleValue() : 0.0;

    String nombre = doc.getString("nombre");
    String detalle = doc.getString("detalle");

    return new RankingUsuarioDto(perfilId, nombre, valor, detalle);
  }
}
