package app.repositories.impl;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.EstadoProceso;
import app.model.entities.EstadoPropuesta;
import app.model.entities.Figurita;
import app.model.entities.MedioDeContacto;
import app.model.entities.Perfil;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioSubastas;
import app.repositories.impl.campos.CamposSubasta;
import com.mongodb.DBRef;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class RepositorioSubastasMongo implements RepositorioSubastas {
  @Autowired
  MongoTemplate mongoTemplate;
  @Override
  public void guardar(Subasta subasta) {
    this.mongoTemplate.save(subasta);
  }

  public void guardar(Subasta subasta, CamposSubasta campos) {
    Update update = new Update();

    if (campos.getOfertas()) {
      update.set("ofertas", subasta.getOfertas());
    }
    if (campos.getFiguritasSolicitadas()) {
      update.set("figuritasSolicitadas", subasta.getFiguritasSolicitadas());
    }

    if (campos.getFechaCierre()) {
      update.set("fechaCierre", subasta.getFechaCierre());
    }

    update.set("avisoFinalEnviado", subasta.isAvisoFinalEnviado());

    mongoTemplate.updateFirst(
        Query.query(Criteria.where("_id").is(subasta.getId())),
        update,
        Subasta.class
    );
  }

  @Override
  public PaginaResultado<Subasta> buscarPorAutor(String perfilId, Integer pagina, Integer limite, CamposSubasta campos) {
    Query query = new Query();

    query.addCriteria(
        Criteria.where("autor.id").is(perfilId)
    );

    this.conCamposCargados(query, campos);

    long count = mongoTemplate.count(query, Subasta.class);

    query.skip((long) pagina * limite);
    query.limit(limite);

    List<Subasta> contenido = mongoTemplate.find(query, Subasta.class).stream().map(this::normalizar).toList();

    return new PaginaResultado<>(
        contenido,
        count,
        (int) Math.ceil((double) count / limite),
        pagina
    );
  }

  @Override
  public PaginaResultado<Subasta> buscarTodos(SubastasFiltro filtros, CamposSubasta campos) {
    List<AggregationOperation> ops = new ArrayList<>();
    List<Criteria> criterios = buildCriterios(filtros);
    if (!criterios.isEmpty()) {
      ops.add(Aggregation.match(new Criteria().andOperator(criterios.toArray(Criteria[]::new))));
    }

    long skip = Math.max(0L, (long) (filtros.pagina() - 1) * filtros.limite());
    List<AggregationOperation> paginaOps = new ArrayList<>();
    paginaOps.add(Aggregation.skip(skip));
    paginaOps.add(Aggregation.limit(filtros.limite()));
    if (!campos.getOfertas()) {
      paginaOps.add(ctx -> new Document("$unset", "ofertas"));
    }
    if (!campos.getFiguritasSolicitadas()) {
      paginaOps.add(ctx -> new Document("$unset", "figuritasSolicitadas"));
    }
    paginaOps.add(Aggregation.lookup("perfiles", "autor.$id", "_id", "_autorDoc"));
    paginaOps.add(Aggregation.unwind("_autorDoc", true));
    paginaOps.add(Aggregation.lookup("usuarios", "_autorDoc.usuario.$id", "_id", "_autorUsuDoc"));
    paginaOps.add(Aggregation.unwind("_autorUsuDoc", true));
    paginaOps.add(Aggregation.lookup("figuritas", "figuritaSubastada.$id", "_id", "_figSubastadaDoc"));
    paginaOps.add(Aggregation.unwind("_figSubastadaDoc", true));
    paginaOps.add(ctx -> new Document("$lookup", new Document()
        .append("from", "perfiles")
        .append("let", new Document("ids", new Document("$map",
            new Document("input", "$ofertas").append("as", "o")
                .append("in", new Document("$getField",
                    new Document("field", new Document("$literal", "$id")).append("input", "$$o.autor"))))))
        .append("pipeline", List.of(new Document("$match",
            new Document("$expr", new Document("$in", List.of("$_id", "$$ids"))))))
        .append("as", "_ofertaAutoresDocs")));
    paginaOps.add(ctx -> new Document("$lookup", new Document()
        .append("from", "usuarios")
        .append("let", new Document("ids", new Document("$map",
            new Document("input", "$_ofertaAutoresDocs").append("as", "p")
                .append("in", new Document("$getField",
                    new Document("field", new Document("$literal", "$id")).append("input", "$$p.usuario"))))))
        .append("pipeline", List.of(new Document("$match",
            new Document("$expr", new Document("$in", List.of("$_id", "$$ids"))))))
        .append("as", "_ofertaUsuariosDocs")));
    paginaOps.add(ctx -> new Document("$lookup", new Document()
        .append("from", "figuritas")
        .append("let", new Document("ids", new Document("$reduce",
            new Document("input", new Document("$map",
                new Document("input", "$ofertas").append("as", "o")
                    .append("in", new Document("$map",
                        new Document("input", "$$o.figuritasOfrecidas").append("as", "f")
                            .append("in", new Document("$getField",
                                new Document("field", new Document("$literal", "$id")).append("input", "$$f")))))))
                .append("initialValue", List.of())
                .append("in", new Document("$concatArrays", List.of("$$value", "$$this"))))))
        .append("pipeline", List.of(new Document("$match",
            new Document("$expr", new Document("$in", List.of("$_id", "$$ids"))))))
        .append("as", "_ofertaFigsDocs")));

    ops.add(Aggregation.facet()
        .and(Aggregation.count().as("n")).as("total")
        .and(paginaOps.toArray(AggregationOperation[]::new)).as("pagina")
    );

    Document result = mongoTemplate.aggregate(
        Aggregation.newAggregation(ops), Subasta.class, Document.class
    ).getMappedResults().stream().findFirst().orElse(new Document());

    List<Document> totalDocs = result.getList("total", Document.class);
    long count = totalDocs != null && !totalDocs.isEmpty()
        ? ((Number) totalDocs.get(0).get("n")).longValue()
        : 0L;

    MongoConverter converter = mongoTemplate.getConverter();
    List<Document> paginaDocs = result.getList("pagina", Document.class);
    List<Subasta> contenido = paginaDocs != null
        ? paginaDocs.stream().map(d -> hydrateSubasta(d, converter)).toList()
        : List.of();

    return new PaginaResultado<>(
        contenido, count,
        (int) Math.ceil((double) count / filtros.limite()),
        filtros.pagina()
    );
  }

  private List<Criteria> buildCriterios(SubastasFiltro filtros) {
    List<Criteria> criterios = new ArrayList<>();
    if (filtros.autorId() != null) {
      criterios.add(Criteria.where("autor.id").is(filtros.autorId()));
    }
    if ("ACTIVA".equals(filtros.estado())) {
      Date ahora = new Date();
      criterios.add(new Criteria().andOperator(
          Criteria.where("fechaInicio").lte(ahora),
          Criteria.where("fechaCierre").gt(ahora)
      ));
    }
    if ("FINALIZADA".equals(filtros.estado())) {
      criterios.add(Criteria.where("fechaCierre").lte(new Date()));
    }
    if (filtros.participanteId() != null) {
      criterios.add(Criteria.where("ofertas").elemMatch(
          Criteria.where("autor.id").is(filtros.participanteId())
              .and("estadoActual.valor").ne(EstadoProceso.CANCELADO)
      ));
    }
    if (filtros.idFiguritaSubastada() != null) {
      criterios.add(Criteria.where("figuritaSubastada.$id").is(filtros.idFiguritaSubastada()));
    }
    if (filtros.idFiguritaOfertada() != null) {
      criterios.add(Criteria.where("ofertas.figuritasOfrecidas.$id").is(filtros.idFiguritaOfertada()));
    }
    return criterios;
  }

  @Override
  public int contar() {
    return (int) this.mongoTemplate.count(new Query(),Subasta.class);
  }

  @Override
  public Subasta buscarPorId(String id, CamposSubasta campos) {
    Query query = new Query();
    query.addCriteria(Criteria.where("_id").is(id));
    this.conCamposCargados(query, campos);
    Subasta subasta = this.mongoTemplate.findOne(query, Subasta.class);

    if(subasta == null) {
      throw new NotFoundException("Subasta no encontrada");
    }
    return this.normalizar(subasta);
  }

  @Override
  public List<Subasta> buscarDondeParticipa(String userId, CamposSubasta campos) {
    Query queryPerfil = new Query(Criteria.where("usuario.id").is(userId));

    Perfil perfil = mongoTemplate.findOne(queryPerfil, Perfil.class);

    if (perfil == null) return List.of();

    Query querySubastas = new Query(
        Criteria.where("ofertas").elemMatch(
            Criteria.where("$id").is(perfil.getId())
        )
    );
    this.conCamposCargados(querySubastas, campos);

    return mongoTemplate.find(querySubastas, Subasta.class).stream().map(this::normalizar).toList();
  }
  @Override
  public List<Subasta> buscarActivasPorFiguritasSubastadas(List<String> figuritaIds) {
    Date ahora = new Date();

    Query query = new Query();
    query.addCriteria(new Criteria().andOperator(
        Criteria.where("figuritaSubastada.$id").in(figuritaIds),
        Criteria.where("fechaInicio").lte(ahora),
        Criteria.where("fechaCierre").gt(ahora)
    ));

    return mongoTemplate.find(query, Subasta.class);
  }

  private void conCamposCargados(Query query, CamposSubasta campos) {
    if(!campos.getOfertas()) {
      query.fields().exclude("ofertas");
    }
    if(!campos.getFiguritasSolicitadas()) {
      query.fields().exclude("figuritasSolicitadas");
    }
  }

  @Override
  public int contarActivasConOfertaPendiente(String figuritaId, String perfilId) {
    Date ahora = new Date();
    Query query = new Query();
    query.addCriteria(new Criteria().andOperator(
        Criteria.where("figuritaSubastada.$id").is(figuritaId),
        Criteria.where("fechaInicio").lte(ahora),
        Criteria.where("fechaCierre").gt(ahora),
        Criteria.where("ofertas").elemMatch(
            new Criteria().andOperator(
                Criteria.where("autor.id").is(perfilId),
                new Criteria().orOperator(
                    Criteria.where("estadoActual.valor").is(EstadoProceso.PENDIENTE),
                    Criteria.where("estadoActual.valor").is(EstadoProceso.SELECCIONADO)
                )
            )
        )
    ));
    return (int) mongoTemplate.count(query, Subasta.class);
  }

  private Subasta normalizar(Subasta subasta) {
    if(subasta.getOfertas() == null) {
      subasta.setOfertas(new ArrayList<>());
    }
    if(subasta.getFiguritasSolicitadas() == null) {
      subasta.setFiguritasSolicitadas(new ArrayList<>());
    }
    return subasta;
  }

  private Subasta hydrateSubasta(Document doc, MongoConverter converter) {
    String id = doc.get("_id") != null ? doc.get("_id").toString() : null;
    LocalDateTime fechaInicio = toLocalDateTime(doc.get("fechaInicio"));
    LocalDateTime fechaCierre = toLocalDateTime(doc.get("fechaCierre"));
    Perfil autor = hydratePerfil(
        doc.get("_autorDoc", Document.class),
        doc.get("_autorUsuDoc", Document.class),
        converter);
    Document figSubDoc = doc.get("_figSubastadaDoc", Document.class);
    Figurita figuritaSubastada = figSubDoc != null ? converter.read(Figurita.class, figSubDoc) : null;
    Map<String, Perfil> autoresPorId = buildPerfilesMap(
        doc.getList("_ofertaAutoresDocs", Document.class),
        doc.getList("_ofertaUsuariosDocs", Document.class),
        converter);
    Map<String, Figurita> figsPorId = buildFiguritasMap(
        doc.getList("_ofertaFigsDocs", Document.class), converter);
    ArrayList<Propuesta> ofertas = buildOfertas(
        doc.getList("ofertas", Document.class),
        autor, figuritaSubastada, autoresPorId, figsPorId, converter);
    return Subasta.builder()
        .id(id)
        .fechaInicio(fechaInicio)
        .fechaCierre(fechaCierre)
        .autor(autor)
        .figuritaSubastada(figuritaSubastada)
        .figuritasSolicitadas(new ArrayList<>())
        .calificacionMinimaSolicitada(doc.getInteger("calificacionMinimaSolicitada", 1))
        .avisoFinalEnviado(Boolean.TRUE.equals(doc.getBoolean("avisoFinalEnviado")))
        .ofertas(ofertas)
        .build();
  }

  private static LocalDateTime toLocalDateTime(Object value) {
    if (value instanceof Date d) return d.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
    if (value instanceof LocalDateTime ldt) return ldt;
    return null;
  }

  private static String dbRefId(Object dbRefValue) {
    if (dbRefValue instanceof DBRef dbRef) return dbRef.getId() != null ? dbRef.getId().toString() : null;
    if (dbRefValue instanceof Document doc) { Object id = doc.get("$id"); return id != null ? id.toString() : null; }
    return null;
  }

  private Map<String, Perfil> buildPerfilesMap(
      List<Document> perfilDocs, List<Document> usuarioDocs, MongoConverter converter) {
    if (perfilDocs == null) return Map.of();
    Map<String, Document> ususPorId = new HashMap<>();
    if (usuarioDocs != null) {
      for (Document u : usuarioDocs) {
        Object uid = u.get("_id");
        if (uid != null) ususPorId.put(uid.toString(), u);
      }
    }
    Map<String, Perfil> result = new HashMap<>();
    for (Document p : perfilDocs) {
      Object pid = p.get("_id");
      if (pid == null) continue;
      String usuId = dbRefId(p.get("usuario"));
      result.put(pid.toString(), hydratePerfil(p, ususPorId.get(usuId), converter));
    }
    return result;
  }

  private Map<String, Figurita> buildFiguritasMap(List<Document> figDocs, MongoConverter converter) {
    if (figDocs == null) return Map.of();
    Map<String, Figurita> result = new HashMap<>();
    for (Document d : figDocs) {
      Object id = d.get("_id");
      if (id != null) result.put(id.toString(), converter.read(Figurita.class, d));
    }
    return result;
  }

  private ArrayList<Propuesta> buildOfertas(
      List<Document> ofertaDocs, Perfil subastaAutor, Figurita figuritaSubastada,
      Map<String, Perfil> autoresPorId, Map<String, Figurita> figsPorId,
      MongoConverter converter) {
    if (ofertaDocs == null) return new ArrayList<>();
    return new ArrayList<>(ofertaDocs.stream().map(of -> {
      String ofId = of.get("_id") != null ? of.get("_id").toString() : null;
      Perfil autorOferta = autoresPorId.get(dbRefId(of.get("autor")));
      List<Object> figRefs = of.getList("figuritasOfrecidas", Object.class);
      List<Figurita> figsOfrecidas = figRefs != null
          ? figRefs.stream().map(r -> figsPorId.get(dbRefId(r))).filter(Objects::nonNull).collect(Collectors.toList())
          : List.of();
      List<Document> estadoDocs = of.getList("estado", Document.class);
      List<EstadoPropuesta> estados = estadoDocs != null
          ? estadoDocs.stream().map(d -> converter.read(EstadoPropuesta.class, d)).collect(Collectors.toList())
          : new ArrayList<>();
      Document estadoActualDoc = of.get("estadoActual", Document.class);
      EstadoPropuesta estadoActual = estadoActualDoc != null
          ? converter.read(EstadoPropuesta.class, estadoActualDoc)
          : (estados.isEmpty() ? null : estados.get(estados.size() - 1));
      return new Propuesta(ofId, autorOferta, subastaAutor,
          figsOfrecidas, figuritaSubastada, new ArrayList<>(estados), estadoActual);
    }).toList());
  }

  private Perfil hydratePerfil(Document perfilDoc, Document usuarioDoc, MongoConverter converter) {
    if (perfilDoc == null) return null;
    String id = perfilDoc.get("_id") != null ? perfilDoc.get("_id").toString() : null;
    String nombre = perfilDoc.getString("nombre");
    Number calMed = perfilDoc.get("calificacionMedia", Number.class);
    double calificacionMedia = calMed != null ? calMed.doubleValue() : 0.0;
    int cantidadCalificaciones = perfilDoc.getInteger("cantidadCalificaciones", 0);
    List<Document> mediosDocs = perfilDoc.getList("mediosDeContacto", Document.class);
    List<MedioDeContacto> medios = mediosDocs != null
        ? mediosDocs.stream().map(d -> converter.read(MedioDeContacto.class, d)).toList()
        : List.of();
    Usuario usuario = usuarioDoc != null ? converter.read(Usuario.class, usuarioDoc) : null;
    return new Perfil(id, usuario, nombre, null, medios, calificacionMedia, cantidadCalificaciones);
  }

  @Override
  public List<Subasta> buscarActivas() {

      Date ahora = new Date();
      Query query = new Query();
      query.addCriteria(
              new Criteria().andOperator(
                      Criteria.where("fechaInicio").lte(ahora),
                      Criteria.where("fechaCierre").gt(ahora)
              )
      );
      return mongoTemplate.find(query, Subasta.class)
              .stream()
              .map(this::normalizar)
              .toList();
  }

  @Override
  public long contarSubastasActivas() {
      Date ahora = new Date();
      Query query = new Query(new Criteria().andOperator(
          Criteria.where("fechaInicio").lte(ahora),
          Criteria.where("fechaCierre").gt(ahora)
      ));
      return mongoTemplate.count(query, Subasta.class);
  }
}
