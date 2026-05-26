package app.repositories.impl;

import com.mongodb.DBRef;
import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.EstadoProceso;
import app.model.entities.Perfil;
import app.model.entities.Subasta;
import app.repositories.RepositorioSubastas;
import app.repositories.impl.campos.CamposSubasta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        Criteria.where("autor").is(perfilId)
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
    Query query = new Query();

    this.conCamposCargados(query, campos);

    if (filtros.autorId() != null) {
      query.addCriteria(
          Criteria.where("autor").is(filtros.autorId())
      );
    }

    if ("ACTIVA".equals(filtros.estado())) {
      Date ahora = new Date();
      query.addCriteria(
          new Criteria().andOperator(
              Criteria.where("fechaInicio").lte(ahora),
              Criteria.where("fechaCierre").gt(ahora)
          )
      );
    }

    if ("FINALIZADA".equals(filtros.estado())) {
      Date ahora = new Date();
      query.addCriteria(
          Criteria.where("fechaCierre").lte(ahora)
      );
    }
    if (filtros.participanteId() != null) {
      DBRef autorRef = new DBRef("perfiles", filtros.participanteId());
      query.addCriteria(
          Criteria.where("ofertas").elemMatch(
              Criteria.where("autor").is(autorRef)
                  .and("estadoActual.valor").ne(EstadoProceso.CANCELADO)
          )
      );
    }

    long count = mongoTemplate.count(query, Subasta.class);

    query.skip((long) (filtros.pagina() - 1) * filtros.limite());
    query.limit(filtros.limite());

    List<Subasta> contenido = mongoTemplate.find(query, Subasta.class).stream().map(this::normalizar).toList();

    return new PaginaResultado<>(
        contenido,
        count,
        (int) Math.ceil((double) count / filtros.limite()),
        filtros.pagina()
    );
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

  private void conCamposCargados(Query query, CamposSubasta campos) {
    if(!campos.getOfertas()) {
      query.fields().exclude("ofertas");
    }
    if(!campos.getFiguritasSolicitadas()) {
      query.fields().exclude("figuritasSolicitadas");
    }
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
}
