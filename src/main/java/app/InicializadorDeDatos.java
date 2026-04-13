package app;

import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Propuesta;
import app.model.entities.Seleccion;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDeDatos implements CommandLineRunner {

    private final RepositorioUsuarios usuarios;
    private final RepositorioPropuestas propuestas;
    private final RepositorioSubastas subastas;

    public InicializadorDeDatos(RepositorioUsuarios usuarios,
                                RepositorioPropuestas propuestas,
                                RepositorioSubastas subastas) {
        this.usuarios = usuarios;
        this.propuestas = propuestas;
        this.subastas = subastas;
    }

    @Override
    public void run(String... args) {
        Figurita messi     = new Figurita("ARG-10", 10, "Messi",     Seleccion.ARGENTINA);
        Figurita diMaria   = new Figurita("ARG-11", 11, "Di María",  Seleccion.ARGENTINA);
        Figurita lautaro   = new Figurita("ARG-9",   9, "Lautaro",   Seleccion.ARGENTINA);
        Figurita mbappe    = new Figurita("FRA-10", 10, "Mbappé",    Seleccion.FRANCIA);
        Figurita griezmann = new Figurita("FRA-7",   7, "Griezmann", Seleccion.FRANCIA);
        Figurita vinicius  = new Figurita("BRA-10", 10, "Vinicius",  Seleccion.BRASIL);
        Figurita pedri     = new Figurita("ESP-10", 10, "Pedri",     Seleccion.ESPAÑA);
        Figurita kroos     = new Figurita("GER-8",   8, "Kroos",     Seleccion.ALEMANIA);

        cargarUsuarios(messi, diMaria, lautaro, mbappe, griezmann, vinicius, pedri, kroos);
        cargarPropuestas(messi, diMaria, griezmann, mbappe, vinicius);
        cargarSubastas(griezmann, vinicius);
    }

    private void cargarUsuarios(Figurita messi, Figurita diMaria, Figurita lautaro,
                                Figurita mbappe, Figurita griezmann, Figurita vinicius,
                                Figurita pedri, Figurita kroos) {
        // Lucas: tiene Messi y Di María repetidas, le falta Mbappé y Vinicius
        Coleccion coleccionLucas = new Coleccion();
        coleccionLucas.getRepetidas().add(new FiguritaIntercambiable(messi,    3, List.of(MetodoIntercambio.INTERCAMBIO)));
        coleccionLucas.getRepetidas().add(new FiguritaIntercambiable(diMaria,  2, List.of(MetodoIntercambio.INTERCAMBIO)));
        coleccionLucas.getFaltantes().add(mbappe);
        coleccionLucas.getFaltantes().add(vinicius);
        usuarios.save(new Usuario("1000", "Lucas",  coleccionLucas, "+5491100000001", new ArrayList<>()));

        // Sofía: tiene Mbappé y Griezmann repetidas, le falta Messi y Lautaro
        Coleccion coleccionSofia = new Coleccion();
        coleccionSofia.getRepetidas().add(new FiguritaIntercambiable(mbappe,    2, List.of(MetodoIntercambio.INTERCAMBIO)));
        coleccionSofia.getRepetidas().add(new FiguritaIntercambiable(griezmann, 1, List.of(MetodoIntercambio.SUBASTA)));
        coleccionSofia.getFaltantes().add(messi);
        coleccionSofia.getFaltantes().add(lautaro);
        usuarios.save(new Usuario("1001", "Sofía",  coleccionSofia, "+5491100000002", new ArrayList<>()));

        // Matías: tiene Vinicius repetido, le falta Pedri y Kroos
        Coleccion coleccionMatias = new Coleccion();
        coleccionMatias.getRepetidas().add(new FiguritaIntercambiable(vinicius, 4, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA)));
        coleccionMatias.getFaltantes().add(pedri);
        coleccionMatias.getFaltantes().add(kroos);
        usuarios.save(new Usuario("1002", "Matías", coleccionMatias, "+5491100000003", new ArrayList<>()));
    }

    private void cargarPropuestas(Figurita messi, Figurita diMaria,
                                  Figurita griezmann, Figurita mbappe, Figurita vinicius) {
        Usuario lucas  = usuarios.findById("1000");
        Usuario sofia  = usuarios.findById("1001");
        Usuario matias = usuarios.findById("1002");

        // Lucas le ofrece Messi a Sofía a cambio de Mbappé — PENDIENTE
        propuestas.save(new Propuesta("2000", lucas, sofia,  List.of(messi),     mbappe,  EstadoProceso.PENDIENTE));

        // Sofía le ofrece Griezmann a Matías a cambio de Vinicius — ACEPTADO
        propuestas.save(new Propuesta("2001", sofia, matias, List.of(griezmann), vinicius, EstadoProceso.ACEPTADO));

        // Matías le ofrece Vinicius a Lucas a cambio de Di María — RECHAZADO
        propuestas.save(new Propuesta("2002", matias, lucas, List.of(vinicius),  diMaria,  EstadoProceso.RECHAZADO));
    }

    private void cargarSubastas(Figurita griezmann, Figurita vinicius) {
        Usuario sofia  = usuarios.findById("1001");
        Usuario matias = usuarios.findById("1002");

        // Subasta activa: Sofía subasta Griezmann, cierra en 2 días
        subastas.save(new Subasta("3000", sofia,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2),
                griezmann, null));

        // Subasta vencida: Matías subastó Vinicius, ya cerró
        subastas.save(new Subasta("3001", matias,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1),
                vinicius, null));
    }
}
