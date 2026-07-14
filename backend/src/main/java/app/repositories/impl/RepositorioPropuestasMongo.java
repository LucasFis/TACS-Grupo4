package app.repositories.impl;

import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.EstadoProceso;
import app.model.entities.Propuesta;
import app.repositories.RepositorioPropuestas;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Query query = new Query();

        if("RECIBIDAS".equals(filtros.tipo())) {
            query.addCriteria(
                Criteria.where("destinatario.id").is(perfilId)
            );
        }

        if("ENVIADAS".equals(filtros.tipo())) {
            query.addCriteria(
                Criteria.where("autor.id").is(perfilId)
            );
        }

        if (filtros.estado() != null) {
            query.addCriteria(
                Criteria.where("estadoActual.valor").is(filtros.estado())
            );
        }

        if (filtros.idFiguritaBuscada() != null) {
            query.addCriteria(
                Criteria.where("figuritaBuscada.$id").is(filtros.idFiguritaBuscada())
            );
        }

        if (filtros.idFiguritaPropuesta() != null) {
            query.addCriteria(
                Criteria.where("figuritasOfrecidas.$id").is(filtros.idFiguritaPropuesta())
            );
        }

        long count = mongoTemplate.count(query, Propuesta.class);

        query.skip((long) (filtros.pagina() - 1) * filtros.limite());
        query.limit(filtros.limite());

        List<Propuesta> contenido =
            mongoTemplate.find(query, Propuesta.class);

        return new PaginaResultado<>(
            contenido,
            count,
            (int) Math.ceil((double) count / filtros.limite()),
            filtros.pagina()
        );
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