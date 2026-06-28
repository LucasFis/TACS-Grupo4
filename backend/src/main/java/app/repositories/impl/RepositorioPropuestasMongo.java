package app.repositories.impl;

import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.Propuesta;
import app.repositories.RepositorioPropuestas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

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

        long count = mongoTemplate.count(query, Propuesta.class);

        query.skip((long) filtros.pagina() - 1 * filtros.limite());
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
    public List<Propuesta> buscarTodosEstadisticas() {
        return mongoTemplate.findAll(Propuesta.class);
    }

    @Override
    public List<Propuesta> buscarEstadisticasPorRango(LocalDateTime desde, LocalDateTime hasta) {
        Query query = new Query(
            Criteria.where("estado.0.fecha").gte(desde).lte(hasta)
        );
        return mongoTemplate.find(query, Propuesta.class);
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
}