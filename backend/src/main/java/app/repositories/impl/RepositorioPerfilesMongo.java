package app.repositories.impl;

import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Sugerencia;
import app.repositories.RepositorioPerfiles;
import app.repositories.impl.campos.CamposPerfil;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class RepositorioPerfilesMongo implements RepositorioPerfiles {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public void guardar(Perfil perfil) {
    mongoTemplate.save(perfil);
  }

  @Override
  public void guardar(Perfil perfil, CamposPerfil campos) {
    Update update = new Update();

    if (campos.getConMedioDeContacto()) {
      update.set("mediosDeContacto", perfil.getMediosDeContacto());
    }

    Document doc = new Document();
    mongoTemplate.getConverter().write(perfil, doc);
    doc.remove("_id");
    doc.remove("mediosDeContacto");
    doc.remove("coleccion");

    doc.forEach(update::set);

    mongoTemplate.updateFirst(
        Query.query(Criteria.where("_id").is(perfil.getId())),
        update,
        Perfil.class
    );
  }

  @Override
  public Perfil buscarPorId(String id, CamposPerfil campos) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("_id").is(id)
    );

    this.conCamposCargados(query, campos);
    Perfil perfil = mongoTemplate.findOne(query, Perfil.class);

    if( perfil == null) {
      throw new NotFoundException("Perfil no encontrado con id: " + id);
    }
    return this.normalizar(perfil);
  }

  @Override
  public Perfil buscarPorUsuarioId(String usuarioId, CamposPerfil campos) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("usuario.id").is(usuarioId)
    );

    this.conCamposCargados(query, campos);
    Perfil perfil = mongoTemplate.findOne(query, Perfil.class);

    if( perfil == null) {
      throw new NotFoundException("Perfil no encontrado con usuario de id: " + usuarioId);
    }

    return perfil;
  }

  @Override
  public List<Perfil> buscarPorFiguritaFaltante(Figurita figurita, CamposPerfil campos) {
    Query queryColecciones = new Query(
        Criteria.where("faltantes").is(figurita.getId())
    );
    List<String> idsColecciones = mongoTemplate.findDistinct(
            queryColecciones,
            "_id",
            Coleccion.class,
            Object.class
        ).stream()
        .map(Object::toString)
        .toList();

    Query queryPerfiles = new Query(
        Criteria.where("coleccion").in(idsColecciones)
    );
    this.conCamposCargados(queryPerfiles, campos);
    return mongoTemplate.find(queryPerfiles, Perfil.class).stream().map(this::normalizar).toList();
  }

  @Override
  public List<Perfil> buscarTodos(CamposPerfil campos) {
    Query query = new Query();
    this.conCamposCargados(query, campos);
    return mongoTemplate.find(query, Perfil.class);
  }

  @Override
  public long contar() {
    Query query = new Query();
    return mongoTemplate.count(query, Perfil.class);
  }

  @Override
  public PaginaResultado<Sugerencia> generarSugerencias(Coleccion coleccionObjetivo, SugerenciasFiltro filtro) {

    List<String> faltantesObjetivo = coleccionObjetivo.getFaltantes()
        .stream().map(Figurita::getId).toList();

    List<String> repetidasObjetivo = coleccionObjetivo.getRepetidas().stream()
        .map(r -> r.getFigurita().getId())
        .toList();

    List<AggregationOperation> ops = new ArrayList<>();

    ops.add(Aggregation.match(
        Criteria.where("_id").ne(coleccionObjetivo.getId())
    ));

    ops.add(Aggregation.lookup("perfiles", "_id", "coleccion.$id", "perfil"));
    ops.add(Aggregation.unwind("perfil"));

    List<String> repetidasObjetivoConIntercambio = coleccionObjetivo.getRepetidas().stream()
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

    // Count
    List<AggregationOperation> countOps = new ArrayList<>(ops);
    countOps.add(Aggregation.count().as("total"));
    Document countDoc = mongoTemplate.aggregate(
        Aggregation.newAggregation(countOps), "colecciones", Document.class
    ).getUniqueMappedResult();
    int total = countDoc != null ? countDoc.getInteger("total") : 0;

    // Paginación
    List<AggregationOperation> pageOps = new ArrayList<>(ops);
    pageOps.add(Aggregation.skip((long) (filtro.pagina() - 1) * filtro.limite()));
    pageOps.add(Aggregation.limit(filtro.limite()));

    AggregationResults<Document> resultados = mongoTemplate.aggregate(
        Aggregation.newAggregation(pageOps), "colecciones", Document.class
    );

    // Mapeo
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

          return new Sugerencia(perfil, sugeridas, necesarias);
        })
        .toList();

    return new PaginaResultado<>(
        sugerencias,
        total,
        (int) Math.ceil((double) total / filtro.limite()),
        filtro.pagina()
    );
  }

  /**
   * Configura la proyección del query para excluir campos según los flags
   * de {@link CamposPerfil}.
   *
   * @param query  query de MongoDB a modificar
   * @param campos especifica qué campos incluir/excluir
   */
  private void conCamposCargados(Query query, CamposPerfil campos) {
    if(!campos.getConMedioDeContacto()) {
      query.fields().exclude("mediosDeContacto");
    }
  }

  /**
   * Normaliza un perfil asegurando que la lista de medios de contacto
   * no sea {@code null}.
   *
   * @param perfil perfil a normalizar
   * @return el mismo perfil con medios de contacto inicializados
   */
  private Perfil normalizar(Perfil perfil) {
    if(perfil.getMediosDeContacto() == null) {
      perfil.setMediosDeContacto(new ArrayList<>());
    }
    return perfil;
  }
}

