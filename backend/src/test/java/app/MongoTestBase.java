package app;
import app.repositories.RepositorioCalificacion;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioSugerencias;
import app.repositories.RepositorioUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class MongoTestBase {
  @Autowired
  protected RepositorioPerfiles repositorioPerfiles;
  @Autowired
  protected RepositorioPropuestas repositorioPropuestas;
  @Autowired
  protected RepositorioSubastas repositorioSubastas;
  @Autowired
  protected RepositorioNotificaciones repositorioNotificaciones;
  @Autowired
  protected RepositorioCalificacion repositorioCalificacion;
  @Autowired
  protected RepositorioColecciones repositorioColecciones;
  @Autowired
  protected RepositorioFiguritas repositorioFiguritas;
  @Autowired
  protected RepositorioUsuarios repositorioUsuarios;
  @Autowired
  protected RepositorioSugerencias repositorioSugerencias;

  @Autowired
  protected MongoTemplate mongoTemplate;

  @AfterEach
  void limpiarDB() {
    mongoTemplate.getCollectionNames()
        .forEach(col -> mongoTemplate.dropCollection(col));
  }
}
