package app.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * Crea los índices de MongoDB al iniciar la aplicación (idempotente).
 *
 * <p>Nota: los subcampos de los {@code DBRef} (ej. {@code repetidas.figurita.$id})
 * no pueden indexarse porque MongoDB prohíbe nombres de campo que empiecen con
 * {@code $} en las claves de índice.
 */
@Configuration
@RequiredArgsConstructor
public class IndicesMongo {

  private final MongoTemplate mongoTemplate;

  @PostConstruct
  public void crearIndices() {
    mongoTemplate.indexOps("colecciones")
        .ensureIndex(new Index().on("repetidas.metodos", Sort.Direction.ASC));

    mongoTemplate.indexOps("figuritas")
        .ensureIndex(new Index().on("numero", Sort.Direction.ASC));
    mongoTemplate.indexOps("figuritas")
        .ensureIndex(new Index().on("jugador", Sort.Direction.ASC));
    mongoTemplate.indexOps("figuritas")
        .ensureIndex(new Index().on("seleccion", Sort.Direction.ASC));

    mongoTemplate.indexOps("subastas")
        .ensureIndex(new Index()
            .on("fechaCierre", Sort.Direction.ASC)
            .on("fechaInicio", Sort.Direction.ASC));
  }
}
