package app.repositories.impl;

import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.dto.filtros.FiguritasFiltro;
import app.repositories.RepositorioFiguritas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RepositorioFiguritasMongo implements RepositorioFiguritas {
  @Autowired
  MongoTemplate mongoTemplate;

  @Override
  public Figurita buscarPorId(String id) {
    Figurita figurita = this.mongoTemplate.findById(id, Figurita.class);

    if(figurita == null) {
      throw new NotFoundException("Figurita no encontrada");
    }

    return figurita;
  }

  @Override
  public List<Figurita> buscarPorIds(List<String> ids) {
    Query query = new Query();
    query.addCriteria(
        Criteria.where("_id").in(ids)
    );
    return this.mongoTemplate.find(query, Figurita.class);
  }

  @Override
  public List<Figurita> buscarConFiltros(FiguritasFiltro filtros) {
    Query query = new Query();
    if (filtros.id() != null) {
      query.addCriteria(Criteria.where("_id").is(filtros.id()));
    }
    if (filtros.jugador() != null) {
      query.addCriteria(Criteria.where("jugador").regex(filtros.jugador(), "i"));

    }
    if (filtros.numero() != null) {
      query.addCriteria(Criteria.where("numero").is(filtros.numero()));

    }
    if (filtros.seleccion() != null) {
      query.addCriteria(Criteria.where("seleccion").regex(filtros.seleccion(), "i"));
    }

    return this.mongoTemplate.find(query, Figurita.class);
  }

  @Override
  public void guardar(Figurita figurita) {
    this.mongoTemplate.save(figurita);
  }
}
