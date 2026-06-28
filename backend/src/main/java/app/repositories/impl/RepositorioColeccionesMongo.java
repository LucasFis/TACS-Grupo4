package app.repositories.impl;

import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.repositories.projections.FiguritaIntercambiableConPerfil;
import app.repositories.projections.ResumenPerfil;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.FiguritasFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.repositories.RepositorioColecciones;
import app.repositories.impl.campos.CamposColeccion;
import com.mongodb.DBRef;
import com.mongodb.client.result.UpdateResult;
import java.util.stream.Collectors;
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

@Repository
public class RepositorioColeccionesMongo implements RepositorioColecciones {
  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public void guardar(Coleccion coleccion) {
    mongoTemplate.save(coleccion);
  }

  @Override
  public void guardar(Coleccion coleccion, CamposColeccion campos) {
    Update update = new Update();

    if (campos.getConRepetidas()) {
      update.set("repetidas", coleccion.getRepetidas());
    }
    if (campos.getConFaltantes()) {
      List<DBRef> refs = coleccion.getFaltantes().stream()
          .map(f -> new DBRef("figuritas", f.getId()))
          .collect(Collectors.toList());
      update.set("faltantes", refs);
    }

    mongoTemplate.updateFirst(
        Query.query(Criteria.where("_id").is(coleccion.getId())),
        update,
        Coleccion.class
    );
  }

  @Override
  public void agregarFaltante(String colId, Figurita figurita) {
    Query query = Query.query(
        Criteria.where("_id").is(colId)
            .and("faltantes.$id").ne(figurita.getId())
    );
    Update update = new Update().push("faltantes", new DBRef("figuritas", figurita.getId()));

    UpdateResult result = mongoTemplate.updateFirst(query, update, Coleccion.class);

    if (result.getMatchedCount() == 0) {
      throw new BadRequestException("Figurita ya listada como faltante");
    }
  }

  @Override
  public void agregarRepetida(String colId, FiguritaIntercambiable repetida) {
    Query query = Query.query(
        Criteria.where("_id").is(colId)
            .and("repetidas.figurita").is(repetida.getFigurita().getId())
    );
    Update incrementar = new Update().inc("repetidas.$.cantidadExistente", repetida.getCantidadExistente());
    UpdateResult result = mongoTemplate.updateFirst(query, incrementar, Coleccion.class);

    if (result.getMatchedCount() == 0) {
      Query queryPush = Query.query(Criteria.where("_id").is(colId));
      Update push = new Update().push("repetidas", repetida);
      mongoTemplate.updateFirst(queryPush, push, Coleccion.class);
    }
  }

  @Override
  public Coleccion buscarPorId(String colId, CamposColeccion campos) {
    Query query = new Query();
    query.addCriteria(Criteria.where("_id").is(colId));
    this.conCamposCargados(query, campos);

    Coleccion coleccion = mongoTemplate.findOne(query, Coleccion.class);

    if(coleccion == null) {
      throw new NotFoundException("No se encontro la coleccion");
    }

    return this.normalizar(coleccion);
  }

  public FiguritaIntercambiable buscarRepetida(String colId, String figId) {
    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.match(
            Criteria.where("_id").is(colId)
        ),
        Aggregation.unwind("repetidas"),
        Aggregation.match(
            Criteria.where("repetidas.figurita.$id").is(figId)
        ),
        Aggregation.replaceRoot("repetidas")
    );

    AggregationResults<FiguritaIntercambiable> result =
        mongoTemplate.aggregate(
            aggregation,
            "colecciones",
            FiguritaIntercambiable.class
        );

    FiguritaIntercambiable repetida = result.getUniqueMappedResult();

    if (repetida == null) {
      throw new NotFoundException("No se encontró la repetida");
    }

    return repetida;
  }

  public void actualizarRepetida(
      String colId,
      String figId,
      FiguritaIntercambiable repetida
  ) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("_id").is(colId)
            .and("repetidas.figurita.$id").is(figId)
    );

    Update update = new Update()
        .set("repetidas.$.cantidadExistente", repetida.getCantidadExistente())
        .set("repetidas.$.metodos", repetida.getMetodos());

    UpdateResult result =
        mongoTemplate.updateFirst(query, update, Coleccion.class);

    if (result.getMatchedCount() == 0) {
      throw new NotFoundException("No se encontró la repetida");
    }
  }

  @Override
  public Repetidas<FiguritaIntercambiable> buscarRepetidas(String colId, RepetidasFiltro filtros, String colIdFaltantes) {
    int pagina = filtros.pagina();
    int limite = filtros.limite();

    List<AggregationOperation> filtrado = new ArrayList<>();

    if (filtros.metodoIntercambio() != null) {
      filtrado.add(Aggregation.match(
          Criteria.where("repetidas.metodos").is(filtros.metodoIntercambio())
      ));
    }

    if (colIdFaltantes != null) {
      List<String> idsFaltantes = obtenerIdsFaltantes(colIdFaltantes);
      filtrado.add(Aggregation.match(
          Criteria.where("repetidas.figurita.$id").in(idsFaltantes)
      ));
    }

    int cantidadResultadosCrudo = this.contarCampoEnColeccion(colId, "repetidas", filtrado);
    int cantidadResultadosDisponibles = this.sumarDisponibles(colId, "repetidas", filtrado);
    AggregationResults<Document> resultado = this.buscarCampoEnColeccion(colId, "repetidas", filtrado, pagina, limite);


    List<FiguritaIntercambiable> figuritas = this.mapearADominio(resultado);

    PaginaResultado<FiguritaIntercambiable> data =
        new PaginaResultado<>(
            figuritas,
            cantidadResultadosCrudo,
            (int) Math.ceil( (double) cantidadResultadosCrudo / limite),
            limite);

    return new Repetidas<>(cantidadResultadosCrudo, cantidadResultadosDisponibles, data);
  }

  /**
   * Obtiene los IDs de las figuritas faltantes de una colección dada,
   * a partir de los {@code DBRef} almacenados en el array {@code faltantes}.
   *
   * @param colIdFaltantes identificador de la colección de faltantes
   * @return lista de IDs de figuritas faltantes
   */
  private List<String> obtenerIdsFaltantes(String colIdFaltantes) {
    Query query = new Query(Criteria.where("_id").is(colIdFaltantes));
    query.fields().include("faltantes");

    Document coleccion = mongoTemplate.findOne(query, Document.class, "colecciones");
    if (coleccion == null) return List.of();

    List<Object> faltantes = coleccion.getList("faltantes", Object.class);
    if (faltantes == null) return List.of();

    return faltantes.stream()
        .map(ref -> ((DBRef) ref).getId().toString())
        .toList();
  }

  @Override
  public PaginaResultado<Figurita> buscarFaltantes(String colId, FaltantesFiltro filtros) {
    int pagina = filtros.pagina();
    int limite = filtros.limite();

    int cantidadResultados = this.contarCampoEnColeccion(colId, "faltantes", new ArrayList<>());

    List<AggregationOperation> operaciones = new ArrayList<>();
    operaciones.add(Aggregation.match(Criteria.where("_id").is(colId)));
    operaciones.add(Aggregation.unwind("faltantes"));
    operaciones.add(Aggregation.lookup("figuritas", "faltantes.$id", "_id", "figurita"));
    operaciones.add(Aggregation.unwind("figurita"));
    operaciones.add(Aggregation.skip((long) (pagina - 1) * limite));
    operaciones.add(Aggregation.limit(limite));
    operaciones.add(Aggregation.replaceRoot("figurita"));

    Aggregation aggregation = Aggregation.newAggregation(operaciones);
    AggregationResults<Document> resultado = mongoTemplate.aggregate(aggregation, "colecciones", Document.class);

    MongoConverter converter = mongoTemplate.getConverter();
    List<Figurita> figuritas = resultado.getMappedResults()
        .stream()
        .map(doc -> converter.read(Figurita.class, doc))
        .toList();

    return new PaginaResultado<>(figuritas, cantidadResultados,
        (int) Math.ceil((double) cantidadResultados / limite), pagina);
  }

  @Override
  public PaginaResultado<FiguritaIntercambiableConPerfil> buscarIntercambiablesConFiltros(
      FiguritasFiltro filtros, int pagina, int limite) {
    List<AggregationOperation> ops = new ArrayList<>();

    ops.add(Aggregation.lookup("figuritas", "repetidas.figurita.$id", "_id", "repetidas.figurita"));
    ops.add(Aggregation.unwind("repetidas.figurita"));

    if (filtros.tipos() != null && !filtros.tipos().isEmpty()) {
      ops.add(Aggregation.match(
          Criteria.where("repetidas.metodos").all(filtros.tipos())
      ));
    }

    if (filtros.id() != null) {
      ops.add(Aggregation.match(
          Criteria.where("repetidas.figurita._id").is(filtros.id())
      ));
    }
    if (filtros.jugador() != null) {
      ops.add(Aggregation.match(
          Criteria.where("repetidas.figurita.jugador").regex(filtros.jugador(), "i")
      ));
    }
    if (filtros.numero() != null) {
      ops.add(Aggregation.match(
          Criteria.where("repetidas.figurita.numero").is(filtros.numero())
      ));
    }
    if (filtros.seleccion() != null) {
      ops.add(Aggregation.match(
          Criteria.where("repetidas.figurita.seleccion").regex(filtros.seleccion(), "i")
      ));
    }

    AggregationResults<Document> resultado =
        this.buscarIntercambiablesConPerfil(ops, pagina, limite);

    int count = this.contarCampoEnColeccion(null, "repetidas", ops);

    List<FiguritaIntercambiableConPerfil> contenido =
        this.mapearIntercambiablesConPerfil(resultado);

    return new PaginaResultado<>(contenido, count, (int) Math.ceil((double) count / limite), pagina);
  }

  @Override
  public PaginaResultado<FiguritaIntercambiableConPerfil> buscarIntercambiablesPorQuery(
      String q, List<MetodoIntercambio> tipos, int pagina, int limite) {

    String[] terminos = q.trim().toLowerCase().split("\\s+");

    List<AggregationOperation> ops = new ArrayList<>();

    if (tipos != null && !tipos.isEmpty()) {
      ops.add(Aggregation.match(
          Criteria.where("repetidas.metodos").all(tipos)
      ));
    }

    ops.add(Aggregation.lookup("figuritas", "repetidas.figurita.$id", "_id", "repetidas.figurita"));
    ops.add(Aggregation.unwind("repetidas.figurita"));

    for (String termino : terminos) {
      List<Criteria> criteriosPorTermino = new ArrayList<>();
      criteriosPorTermino.add(Criteria.where("repetidas.figurita.jugador").regex(termino, "i"));
      criteriosPorTermino.add(Criteria.where("repetidas.figurita.seleccion").regex(termino, "i"));

      Integer numero = parseNumero(termino);
      if (numero != null) {
        criteriosPorTermino.add(Criteria.where("repetidas.figurita.numero").is(numero));
      }

      ops.add(Aggregation.match(new Criteria().orOperator(criteriosPorTermino)));
    }

    int count = this.contarCampoEnColeccion(null, "repetidas", ops);
    AggregationResults<Document> resultado =
        this.buscarIntercambiablesConPerfil(ops, pagina, limite);
    List<FiguritaIntercambiableConPerfil> contenido =
        this.mapearIntercambiablesConPerfil(resultado);

    return new PaginaResultado<>(contenido, count, (int) Math.ceil((double) count / limite), pagina);
  }

  @Override
  public List<FiguritaIntercambiable> buscarIntercambiablesPorFiguritaIds(List<String> figuritaIds) {
    List<AggregationOperation> ops = new ArrayList<>();
    ops.add(Aggregation.lookup("figuritas", "repetidas.figurita.$id", "_id", "repetidas.figurita"));
    ops.add(Aggregation.unwind("repetidas.figurita"));
    ops.add(Aggregation.match(
        Criteria.where("repetidas.figurita._id").in(figuritaIds)
    ));

    AggregationResults<Document> resultados = this.buscarCampoEnColeccion(null, "repetidas", ops, 1, 100);

    return this.mapearADominio(resultados);
  }

  @Override
  public List<FiguritaIntercambiable> buscarIntercambiablesPorPerfilId(String perfilId) {
    Query query = new Query(Criteria.where("_id").is(perfilId));
    query.fields().include("coleccion");

    Document doc = mongoTemplate.findOne(query, Document.class, "perfiles");
    if (doc == null) {
      return new ArrayList<>();
    }
    DBRef ref = (DBRef) doc.get("coleccion");
    String colId = ref.getId().toString();

    AggregationResults<Document> figuritas = this.buscarCampoEnColeccion(colId, "repetidas", new ArrayList<>(), 1,100);
    return this.mapearADominio(figuritas);
  }

  /**
   * Configura la proyección del query para excluir campos según los flags
   * de {@link CamposColeccion}, optimizando la lectura.
   *
   * @param query  query de MongoDB a modificar
   * @param campos especifica qué campos incluir/excluir
   */
  private void conCamposCargados(Query query, CamposColeccion campos) {
    if(!campos.getConRepetidas()) {
      query.fields().exclude("repetidas");
    }
    if(!campos.getConFaltantes()) {
      query.fields().exclude("faltantes");
    }
  }

  /**
   * Normaliza una colección asegurando que las listas de repetidas y faltantes
   * no sean {@code null}, reemplazándolas con listas vacías si es necesario.
   *
   * @param coleccion colección a normalizar
   * @return la misma colección con listas inicializadas
   */
  private Coleccion normalizar(Coleccion coleccion) {
    if(coleccion.getRepetidas() == null) {
      coleccion.setRepetidas(new ArrayList<>());
    }
    if(coleccion.getFaltantes() == null) {
      coleccion.setFaltantes(new ArrayList<>());
    }
    return coleccion;
  }

  /**
   * Cuenta los elementos de un array embebido en una colección aplicando
   * un pipeline de agregación con {@code $unwind}, filtros opcionales y
   * {@code $count}.
   *
   * @param colId identificador de la colección, o {@code null} para todas
   * @param campo nombre del campo array a contar (ej. "repetidas", "faltantes")
   * @param ops   operaciones de agregación adicionales para filtrar
   * @return cantidad de elementos que cumplen los criterios
   */
  private int contarCampoEnColeccion(String colId, String campo, List<AggregationOperation> ops) {
    List<AggregationOperation> operaciones = new ArrayList<>();

    if(colId != null) {
      operaciones.add(Aggregation.match(Criteria.where("_id").is(colId)));
    }
    operaciones.add(Aggregation.unwind(campo));
    operaciones.addAll(ops);
    operaciones.add(Aggregation.count().as("total"));

    Aggregation countAggregation = Aggregation.newAggregation(operaciones);

    AggregationResults<Document> countResult =
        mongoTemplate.aggregate(countAggregation, "colecciones", Document.class);

    Document countDoc = countResult.getUniqueMappedResult();
    return countDoc != null ? countDoc.getInteger("total") : 0;
  }

  /**
   * Busca elementos de un array embebido con paginación mediante un pipeline
   * de agregación que hace {@code $unwind}, aplica filtros y reemplaza la raíz
   * con el campo deseado.
   *
   * @param colId  identificador de la colección, o {@code null} para todas
   * @param campo  nombre del campo array a recorrer
   * @param ops    operaciones de agregación adicionales para filtrar
   * @param pagina número de página (base 1)
   * @param limite cantidad máxima de resultados
   * @return resultados crudos de la agregación como documentos
   */
  private AggregationResults<Document> buscarCampoEnColeccion(String colId, String campo, List<AggregationOperation> ops, int pagina, int limite) {
    List<AggregationOperation> operaciones = new ArrayList<>();
    if(colId != null) {
      operaciones.add(Aggregation.match(Criteria.where("_id").is(colId)));
    }
    operaciones.add(Aggregation.unwind(campo));
    operaciones.addAll(ops);
    operaciones.add(Aggregation.skip((long) (pagina - 1) * limite));
    operaciones.add(Aggregation.limit(limite));
    operaciones.add(Aggregation.replaceRoot(campo));

    Aggregation aggregation = Aggregation.newAggregation(operaciones);

    return mongoTemplate.aggregate(aggregation, "colecciones", Document.class);
  }

  /**
   * Ejecuta un pipeline de agregación que busca figuritas intercambiables
   * con los datos del perfil propietario. Aplica {@code $unwind} sobre
   * {@code repetidas}, los filtros recibidos, paginación, {@code $lookup}
   * contra la colección {@code perfiles} y proyección final.
   *
   * @param ops    operaciones de agregación para filtrar
   * @param pagina número de página (base 1)
   * @param limite cantidad máxima de resultados
   * @return documentos con los datos de la figurita y el perfil asociado
   */
  private AggregationResults<Document> buscarIntercambiablesConPerfil(
      List<AggregationOperation> ops,
      int pagina,
      int limite
  ) {
    List<AggregationOperation> operaciones = new ArrayList<>();
    operaciones.add(Aggregation.unwind("repetidas"));
    operaciones.addAll(ops);
    operaciones.add(Aggregation.skip((long) (pagina - 1) * limite));
    operaciones.add(Aggregation.limit(limite));
    operaciones.add(Aggregation.lookup(
        "perfiles",
        "repetidas.perfilId",
        "_id",
        "perfil"
    ));
    operaciones.add(Aggregation.unwind("perfil", true));
    operaciones.add(Aggregation.project()
        .and("repetidas").as("figurita")
        .and("perfil._id").as("perfilId")
        .and("perfil.nombre").as("perfilNombre")
        .and("perfil.calificacionMedia").as("perfilCalificacionMedia"));

    return mongoTemplate.aggregate(
        Aggregation.newAggregation(operaciones),
        "colecciones",
        Document.class
    );
  }

  /**
   * Suma la cantidad de ejemplares disponibles (existentes - reservados)
   * de un array embebido, aplicando filtros opcionales mediante agregación.
   *
   * @param colId identificador de la colección
   * @param campo nombre del campo array (ej. "repetidas")
   * @param ops   operaciones de agregación adicionales
   * @return total de ejemplares disponibles
   */
  private int sumarDisponibles(String colId, String campo, List<AggregationOperation> ops) {
    List<AggregationOperation> operaciones = new ArrayList<>();

    operaciones.add(Aggregation.match(Criteria.where("_id").is(colId)));
    operaciones.add(Aggregation.unwind(campo));
    operaciones.addAll(ops);
    operaciones.add(Aggregation.group()
        .sum(campo + ".cantidadExistente").as("totalExistente")
        .sum(campo + ".cantidadReservada").as("totalReservada")
    );

    Aggregation aggregation = Aggregation.newAggregation(operaciones);

    AggregationResults<Document> result =
        mongoTemplate.aggregate(aggregation, "colecciones", Document.class);

    Document doc = result.getUniqueMappedResult();
    if (doc == null) return 0;

    int existente = doc.getInteger("totalExistente", 0);
    int reservada = doc.getInteger("totalReservada", 0);
    return existente - reservada;
  }

  /**
   * Convierte los documentos resultado de una agregación en objetos
   * {@link FiguritaIntercambiable} usando el {@link MongoConverter}.
   *
   * @param resultado resultados de la agregación
   * @return lista de figuritas intercambiables mapeadas
   */
  private List<FiguritaIntercambiable> mapearADominio(AggregationResults<Document> resultado) {
    MongoConverter converter = mongoTemplate.getConverter();

    return resultado.getMappedResults()
        .stream()
        .map(doc -> converter.read(FiguritaIntercambiable.class, doc))
        .toList();
  }

  /**
   * Convierte documentos de agregación en proyecciones {@link FiguritaIntercambiableConPerfil},
   * extrayendo la figurita y los datos de perfil del documento anidado.
   *
   * @param resultado resultados de la agregación
   * @return lista de proyecciones con figurita y resumen del perfil
   */
  private List<FiguritaIntercambiableConPerfil> mapearIntercambiablesConPerfil(
      AggregationResults<Document> resultado
  ) {
    MongoConverter converter = mongoTemplate.getConverter();

    return resultado.getMappedResults().stream()
        .map(documento -> {
          FiguritaIntercambiable figurita = converter.read(
              FiguritaIntercambiable.class,
              documento.get("figurita", Document.class)
          );

          return new FiguritaIntercambiableConPerfil(
              figurita,
              mapearResumenPerfil(documento)
          );
        })
        .toList();
  }

  /**
   * Extrae los campos de resumen de un perfil desde un documento de agregación
   * y los convierte en un {@link ResumenPerfil}.
   *
   * @param documento documento con campos {@code perfilId}, {@code perfilNombre}
   *                  y {@code perfilCalificacionMedia}
   * @return resumen del perfil, o {@code null} si no hay datos
   */
  private ResumenPerfil mapearResumenPerfil(Document documento) {
    Object perfilId = documento.get("perfilId");
    if (perfilId == null) {
      return null;
    }

    Object calificacionMedia = documento.get("perfilCalificacionMedia");
    double valor = calificacionMedia instanceof Number numero
        ? numero.doubleValue()
        : 0.0;

    return new ResumenPerfil(
        perfilId.toString(),
        documento.getString("perfilNombre"),
        valor
    );
  }

  /**
   * Intenta parsear un término como número entero.
   *
   * @param termino cadena a parsear
   * @return el número entero, o {@code null} si no es numérico
   */
  private Integer parseNumero(String termino) {
    try { return Integer.parseInt(termino); }
    catch (NumberFormatException e) { return null; }
  }

  @Override
  public long contarRepetidas(List<MetodoIntercambio> metodos) {

    List<AggregationOperation> operaciones = new ArrayList<>();

    operaciones.add(Aggregation.unwind("repetidas"));

    if (!metodos.isEmpty()) {
      operaciones.add(
          Aggregation.match(
              new Criteria().andOperator(
                  Criteria.where("repetidas.metodos").all(metodos),
                  Criteria.where("repetidas.metodos").size(metodos.size())
              )
          )
      );
    }

    operaciones.add(
        Aggregation.group()
            .sum("repetidas.cantidadExistente")
            .as("total")
    );

    Aggregation aggregation =
        Aggregation.newAggregation(operaciones);

    AggregationResults<Document> resultado =
        mongoTemplate.aggregate(
            aggregation,
            Coleccion.class,
            Document.class
        );

    Document doc = resultado.getUniqueMappedResult();

    Number total = doc != null ? doc.get("total", Number.class) : 0L;

    return total.longValue();
  }
}
