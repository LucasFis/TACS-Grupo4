package app;

import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.EstadoPropuesta;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Propuesta;
import app.model.entities.Rol;
import app.model.entities.Seleccion;
import app.model.entities.Subasta;
import app.model.entities.Perfil;
import app.model.entities.Usuario;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioPerfiles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDeDatos implements CommandLineRunner {

    private final RepositorioPerfiles perfiles;
    private final RepositorioPropuestas propuestas;
    private final RepositorioSubastas subastas;
    private final RepositorioFiguritas figuritas;
    private final RepositorioColecciones colecciones;
    private final RepositorioFiguritasIntercambiables intercambiables;

    public InicializadorDeDatos(RepositorioPerfiles perfiles,
                                RepositorioPropuestas propuestas,
                                RepositorioSubastas subastas,
                                RepositorioColecciones colecciones,
                                RepositorioFiguritas figuritas,
                                RepositorioFiguritasIntercambiables intercambiables) {
        this.perfiles = perfiles;
        this.propuestas = propuestas;
        this.subastas = subastas;
        this.colecciones = colecciones;
        this.figuritas = figuritas;
        this.intercambiables = intercambiables;
    }

    private List<MedioDeContacto> telegram(String numero) {
        return new ArrayList<>(List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero)));
    }

    private Propuesta propuesta(String id, Perfil autor, Perfil destino,
                                List<Figurita> figuritas, Figurita buscada, EstadoProceso estado) {
        return new Propuesta(id, autor, destino, figuritas, buscada,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), estado))));
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

        figuritas.guardar(messi);
        figuritas.guardar(diMaria);
        figuritas.guardar(lautaro);
        figuritas.guardar(vinicius);

        cargarPerfiles(messi, diMaria, lautaro, mbappe, griezmann, vinicius, pedri, kroos);
        cargarPropuestas(messi, diMaria, griezmann, mbappe, vinicius);
        cargarSubastas(griezmann, vinicius);
    }

    private void cargarPerfiles(Figurita messi, Figurita diMaria, Figurita lautaro,
                                Figurita mbappe, Figurita griezmann, Figurita vinicius,
                                Figurita pedri, Figurita kroos) {
        // Lucas
        Coleccion coleccionLucas = new Coleccion();
        coleccionLucas.setId("1");
        FiguritaIntercambiable interMessi   = new FiguritaIntercambiable(messi,   3, List.of(MetodoIntercambio.INTERCAMBIO), "1000");
        FiguritaIntercambiable interDiMaria = new FiguritaIntercambiable(diMaria, 2, List.of(MetodoIntercambio.SUBASTA),     "1000");
        coleccionLucas.getRepetidas().add(interMessi);
        coleccionLucas.getRepetidas().add(interDiMaria);
        coleccionLucas.getFaltantes().add(mbappe);
        coleccionLucas.getFaltantes().add(vinicius);
        intercambiables.guardar(interMessi);
        intercambiables.guardar(interDiMaria);
        colecciones.guardar(coleccionLucas);
        perfiles.guardar(new Perfil("1000", new Usuario("u-1000",  Rol.USUARIO), "Lucas",
            coleccionLucas, telegram("@lucas"), new ArrayList<>()));

        // Sofía
        Coleccion coleccionSofia = new Coleccion();
        coleccionSofia.setId("2");
        FiguritaIntercambiable interMbappe    = new FiguritaIntercambiable(mbappe,    2, List.of(MetodoIntercambio.INTERCAMBIO), "1001");
        FiguritaIntercambiable interGriezmann = new FiguritaIntercambiable(griezmann, 1, List.of(MetodoIntercambio.SUBASTA),     "1001");
        coleccionSofia.getRepetidas().add(interMbappe);
        coleccionSofia.getRepetidas().add(interGriezmann);
        coleccionSofia.getFaltantes().add(messi);
        coleccionSofia.getFaltantes().add(lautaro);
        intercambiables.guardar(interMbappe);
        intercambiables.guardar(interGriezmann);
        colecciones.guardar(coleccionSofia);
        perfiles.guardar(new Perfil("1001", new Usuario("u-1001", Rol.USUARIO), "Sofía",
            coleccionSofia, telegram("@sofia"), new ArrayList<>()));

        // Matías
        Coleccion coleccionMatias = new Coleccion();
        coleccionMatias.setId("3");
        FiguritaIntercambiable interVinicius = new FiguritaIntercambiable(vinicius, 1, List.of(MetodoIntercambio.INTERCAMBIO), "1002");
        coleccionMatias.getRepetidas().add(interVinicius);
        coleccionMatias.getFaltantes().add(pedri);
        coleccionMatias.getFaltantes().add(kroos);
        intercambiables.guardar(interVinicius);
        colecciones.guardar(coleccionMatias);
        perfiles.guardar(new Perfil("1002", new Usuario("u-1002",  Rol.USUARIO), "Matías",
            coleccionMatias, telegram("@matias"), new ArrayList<>()));

        // Juan
        Coleccion coleccionJuan = new Coleccion();
        coleccionJuan.setId("4");
        FiguritaIntercambiable interPedri = new FiguritaIntercambiable(pedri, 1, List.of(MetodoIntercambio.INTERCAMBIO), "1003");
        coleccionJuan.getRepetidas().add(interPedri);
        coleccionJuan.getFaltantes().add(pedri);
        coleccionJuan.getFaltantes().add(kroos);
        intercambiables.guardar(interPedri);
        colecciones.guardar(coleccionJuan);
        perfiles.guardar(new Perfil("1003", new Usuario("u-1003",  Rol.USUARIO), "Juan",
            coleccionJuan, telegram("@juan"), new ArrayList<>()));
    }

    private void cargarPropuestas(Figurita messi, Figurita diMaria,
                                  Figurita griezmann, Figurita mbappe, Figurita vinicius) {
        Perfil lucas  = perfiles.buscarPorId("1000");
        Perfil sofia  = perfiles.buscarPorId("1001");
        Perfil matias = perfiles.buscarPorId("1002");

        propuestas.guardar(propuesta("2000", lucas,  sofia,  List.of(messi),     mbappe,   EstadoProceso.PENDIENTE));
        propuestas.guardar(propuesta("2001", sofia,  matias, List.of(griezmann), vinicius, EstadoProceso.ACEPTADO));
        propuestas.guardar(propuesta("2002", matias, lucas,  List.of(vinicius),  diMaria,  EstadoProceso.RECHAZADO));
    }

    private void cargarSubastas(Figurita griezmann, Figurita vinicius) {
        Perfil sofia  = perfiles.buscarPorId("1001");
        Perfil matias = perfiles.buscarPorId("1002");

        subastas.guardar(new Subasta("3000", sofia,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2),
            griezmann, new ArrayList<>(), new ArrayList<>(), 0, false));

        subastas.guardar(new Subasta("3001", matias,
            LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1),
            vinicius, new ArrayList<>(), new ArrayList<>(), 0, false));
    }
}