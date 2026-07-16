package app.repositories.impl;

import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.EstadoProceso;
import app.model.entities.EstadoPropuesta;
import app.model.entities.Figurita;
import app.model.entities.MedioDeContacto;
import app.model.entities.Perfil;
import app.model.entities.Propuesta;
import app.model.entities.Usuario;
import app.repositories.RepositorioPropuestas;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPropuestasMongo implements RepositorioPropuestas {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void guardar(Propuesta propuesta) {
        this.mongoTemplate.save(propuesta);
    }

    @Override
    public PaginaResultado<Propuesta> buscarTodos(String perfilId, PropuestasFiltro filtros) {
        List<AggregationOperation> pipeline = new ArrayList<>(buildMatchCriterios(perfilId, filtros));
        long skip = Math.max(0L, (long) (filtros.pagina() - 1) * filtros.limite());

        pipeline.add(Aggregation.facet()
            .and(Aggregation.count().as("n")).as("total")
            .and(
                Aggregation.skip(skip),
                Aggregation.limit(filtros.limite()),
                Aggregation.lookup("perfiles", "autor.$id", "_id", "autorDoc"),
                Aggregation.unwind("autorDoc", true),
                Aggregation.lookup("usuarios", "autorDoc.usuario.$id", "_id", "autorUsuDoc"),
                Aggregation.unwind("autorUsuDoc", true),
                Aggregation.lookup("perfiles", "destinatario.$id", "_id", "destDoc"),
                Aggregation.unwind("destDoc", true),
                Aggregation.lookup("usuarios", "destDoc.usuario.$id", "_id", "destUsuDoc"),
                Aggregation.unwind("destUsuDoc", true),
                Aggregation.lookup("figuritas", "figuritaBuscada.$id", "_id", "figBuscadaDoc"),
                Aggregation.unwind("figBuscadaDoc", true),
                Aggregation.lookup("figuritas", "figuritasOfrecidas.$id", "_id", "figsOfrecidasDocs")
            ).as("pagina")
        );

        Document result = mongoTemplate.aggregate(
            Aggregation.newAggregation(pipeline), "propuestas", Document.class)
            .getMappedResults().stream().findFirst().orElse(new Document());

        List<Document> totalDocs = result.getList("total", Document.class);
        long count = totalDocs != null && !totalDocs.isEmpty()
            ? ((Number) totalDocs.get(0).get("n")).longValue()
            : 0L;

        MongoConverter converter = mongoTemplate.getConverter();
        List<Document> paginaDocs = result.getList("pagina", Document.class);
        List<Propuesta> contenido = paginaDocs != null
            ? paginaDocs.stream().map(doc -> hydratePropuesta(doc, converter)).toList()
            : List.of();

        int cantidadPaginas = (int) Math.ceil((double) count / filtros.limite());
        return new PaginaResultado<>(contenido, count, cantidadPaginas, filtros.pagina());
    }

    private List<AggregationOperation> buildMatchCriterios(String perfilId, PropuestasFiltro filtros) {
        List<Criteria> criterios = new ArrayList<>();

        if("RECIBIDAS".equals(filtros.tipo())) {
            criterios.add(criterioDbRefId("destinatario.$id", perfilId));
        } else if("ENVIADAS".equals(filtros.tipo())) {
            criterios.add(criterioDbRefId("autor.$id", perfilId));
        }

        if (filtros.estado() != null) {
            criterios.add(Criteria.where("estadoActual.valor").is(filtros.estado()));
        }
        if (filtros.idFiguritaBuscada() != null) {
            criterios.add(criterioDbRefId("figuritaBuscada.$id", filtros.idFiguritaBuscada()));
        }
        if (filtros.idFiguritaPropuesta() != null) {
            criterios.add(criterioDbRefId("figuritasOfrecidas.$id", filtros.idFiguritaPropuesta()));
        }

        if (criterios.isEmpty())
            return List.of();

        return List.of(Aggregation.match(new Criteria().andOperator(criterios.toArray(Criteria[]::new))));
    }

    private Propuesta hydratePropuesta(Document doc, MongoConverter converter) {
        Perfil autor = hydratePerfil(
            doc.get("autorDoc", Document.class),
            doc.get("autorUsuDoc", Document.class),
            converter);

        Perfil dest = hydratePerfil(
            doc.get("destDoc", Document.class),
            doc.get("destUsuDoc", Document.class),
            converter);

        Document figBuscadaDoc = doc.get("figBuscadaDoc", Document.class);
        Figurita figuritaBuscada = figBuscadaDoc != null
            ? converter.read(Figurita.class, figBuscadaDoc)
            : null;

        List<Document> figsOfrecidasDocs = doc.getList("figsOfrecidasDocs", Document.class);
        List<Figurita> figuritasOfrecidas = figsOfrecidasDocs != null
            ? figsOfrecidasDocs.stream().map(d -> converter.read(Figurita.class, d)).toList()
            : List.of();

        List<Document> estadoDocs = doc.getList("estado", Document.class);
        List<EstadoPropuesta> estados = estadoDocs != null
            ? estadoDocs.stream().map(d -> converter.read(EstadoPropuesta.class, d)).toList()
            : List.of();

        String id = doc.get("_id") != null
            ? doc.get("_id").toString()
            : null;

        EstadoPropuesta estadoActual = estados.isEmpty()
            ? null
            : estados.get(estados.size() - 1);

        ArrayList<EstadoPropuesta> listaEstados = new ArrayList<>(estados);
        return new Propuesta(id, autor, dest, figuritasOfrecidas, figuritaBuscada, listaEstados, estadoActual);
    }

    private Perfil hydratePerfil(Document perfilDoc, Document usuarioDoc, MongoConverter converter) {
        if (perfilDoc == null)
            return null;

        String id = perfilDoc.get("_id") != null
            ? perfilDoc.get("_id").toString()
            : null;

        String nombre = perfilDoc.getString("nombre");
        Number calMed = perfilDoc.get("calificacionMedia", Number.class);

        double calificacionMedia = calMed != null
            ? calMed.doubleValue()
            : 0.0;

        int cantidadCalificaciones = perfilDoc.getInteger("cantidadCalificaciones", 0);

        List<Document> mediosDocs = perfilDoc.getList("mediosDeContacto", Document.class);

        List<MedioDeContacto> medios = mediosDocs != null
            ? mediosDocs.stream().map(d -> converter.read(MedioDeContacto.class, d)).toList()
            : List.of();

        Usuario usuario = usuarioDoc != null ? converter.read(Usuario.class, usuarioDoc) : null;

        return new Perfil(id, usuario, nombre, null, medios, calificacionMedia, cantidadCalificaciones);
    }

    private Criteria criterioDbRefId(String campo, String id) {
        try {
            return Criteria.where(campo).in(Arrays.asList(id, new ObjectId(id)));
        } catch (IllegalArgumentException e) {
            return Criteria.where(campo).is(id);
        }
    }

    @Override
    public Map<EstadoProceso, Long> contarPorEstadoEnRango(LocalDateTime desde, LocalDateTime hasta) {
        return mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(Criteria.where("estado.0.fecha").gte(desde).lte(hasta)),
                Aggregation.group("$estadoActual.valor").count().as("total")
            ),
            "propuestas", Document.class
        ).getMappedResults().stream().collect(Collectors.toMap(
            d -> EstadoProceso.valueOf(d.getString("_id")),
            d -> ((Number) d.get("total")).longValue()
        ));
    }

    @Override
    public Propuesta buscarPorId(String id){
        Propuesta propuesta = this.mongoTemplate.findById(id, Propuesta.class);

        if(propuesta == null) {
            throw new NotFoundException("Propuesta no encontrada");
        }
        return propuesta;
    }

    @Override
    public int contar() {
        return (int) this.mongoTemplate.count(new Query(), Propuesta.class);
    }

    @Override
    public int contarConflictos(String figuritaId, String perfilId, String excluirPropuestaId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").ne(excluirPropuestaId));
        query.addCriteria(Criteria.where("estadoActual.valor").is(EstadoProceso.PENDIENTE));
        query.addCriteria(new Criteria().orOperator(
            Criteria.where("figuritaBuscada.$id").is(figuritaId)
                .and("autor.id").is(perfilId),
            Criteria.where("figuritasOfrecidas.$id").is(figuritaId)
                .and("destinatario.id").is(perfilId)
        ));
        return (int) this.mongoTemplate.count(query, Propuesta.class);
    }
}
