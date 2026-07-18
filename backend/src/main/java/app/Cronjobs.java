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
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.servicios.ServicioNotificacion;
import java.time.Duration;
import java.time.LocalDateTime;
import app.repositories.RepositorioSubastas;

import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class Cronjobs implements ApplicationRunner {

    private final RepositorioPerfiles repositorioPerfiles;
    private final RepositorioSugerencias repositorioSugerencias;
    private final RepositorioSubastas repoSubasta;
    private final ServicioNotificacion notificacionService;

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

    // Corre cada 10 minutos. Se avisan todas las subastas que terminan en <1hora.
    @Scheduled(cron = "0 */10 * * * *")
    public void avisarSubastasPorFinalizar() {

        List<Subasta> subastas = repoSubasta.buscarActivas();

        for (Subasta subasta : subastas) {

            if (subasta.isAvisoFinalEnviado())
                continue;

            if (subasta.necesitaAvisoFinal()) {

                List<Perfil> interesados = subasta.getOfertas()
                        .stream()
                        .map(Propuesta::getAutor)
                        .toList();

                try {
                    notificacionService.notificarInteresados(
                            interesados,
                            "La subasta de la figurita #" +
                                    subasta.getFiguritaSubastada().getNumero() + " " +
                                subasta.getFiguritaSubastada().getJugador() +
                                    " finaliza en menos de una hora.",
                            "/subastas/" + subasta.getId()
                    );

                    subasta.setAvisoFinalEnviado(true);
                    repoSubasta.guardar(subasta);

                } catch (Exception e) {
                    e.printStackTrace(); //lo mejor sería loguearlo.
                }
            }
        }
    }
  }