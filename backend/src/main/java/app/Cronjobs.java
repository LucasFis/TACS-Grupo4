package app;

import app.model.entities.Perfil;
import app.model.entities.Sugerencia;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioSugerencias;
import app.repositories.impl.campos.CamposPerfil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class Cronjobs implements ApplicationRunner {

  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioSugerencias repositorioSugerencias;

  // Corre al inicio
  @Override
  public void run(ApplicationArguments args) throws Exception {
    crearSugerencias();
  }

  // Corre todos los días a las 3am
  @Scheduled(cron = "0 0 3 * * *")
  public void crearSugerencias() {
    this.repositorioSugerencias.eliminacionProgramada();

    List<Perfil> perfiles = this.repositorioPerfiles.buscarTodos(new CamposPerfil(false));
    perfiles.forEach(perfil -> {

      List<Sugerencia> sugerencias = this.repositorioSugerencias.generarSugerencias(perfil);
      this.repositorioSugerencias.guardar(sugerencias);
    });
  }
}