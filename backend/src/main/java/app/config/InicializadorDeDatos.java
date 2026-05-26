package app.config;

import app.model.entities.Figurita;
import app.model.entities.Coleccion;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Propuesta;
import app.model.entities.Rol;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioCalificacion;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class InicializadorDeDatos implements CommandLineRunner {

  private final RepositorioPerfiles perfiles;
  private final RepositorioPropuestas propuestas;
  private final RepositorioSubastas subastas;
  private final RepositorioFiguritas figuritas;
  private final RepositorioColecciones colecciones;
  private final RepositorioUsuarios usuarios;
  private final RepositorioCalificacion calificaciones;

  public InicializadorDeDatos(RepositorioPerfiles perfiles,
                              RepositorioPropuestas propuestas,
                              RepositorioSubastas subastas,
                              RepositorioColecciones colecciones,
                              RepositorioFiguritas figuritas,
                              RepositorioUsuarios usuarios,
                              RepositorioCalificacion calificaciones) {
    this.perfiles = perfiles;
    this.propuestas = propuestas;
    this.subastas = subastas;
    this.colecciones = colecciones;
    this.figuritas = figuritas;
    this.usuarios = usuarios;
    this.calificaciones = calificaciones;
  }

  // ─── HELPERS ──────────────────────────────────────────────────────────────

  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private List<MedioDeContacto> telegram(String usuario) {
    return new ArrayList<>(List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, usuario)));
  }

  private Usuario crearUsuario(String nombre, String password, Rol rol) {
    Usuario u = new Usuario(nombre, passwordEncoder.encode(password), rol);
    usuarios.guardar(u);
    return u;
  }

  private Perfil crearPerfil(String id, String nombre, Usuario usuario,
                             Coleccion coleccion, String telegramHandle) {
    Perfil p = Perfil.builder()
        .id(id)
        .usuario(usuario)
        .nombre(nombre)
        .coleccion(coleccion)
        .mediosDeContacto(telegram(telegramHandle))
        .build();
    perfiles.guardar(p);
    return p;
  }

  private Coleccion crearColeccion() {
    Coleccion c = new Coleccion();
    colecciones.guardar(c);
    return c;
  }

  private void agregarRepetidaNueva(Coleccion coleccion, Figurita figurita,
                                    int cantidad,
                                    List<MetodoIntercambio> metodos, String perfilId) {
    coleccion.getRepetidas().add(
        new FiguritaIntercambiable(figurita, cantidad, metodos, perfilId)
    );
  }

  // ─── ENTRY POINT ──────────────────────────────────────────────────────────

  @Override
  public void run(String... args) {
    if (subastas.contar() > 0) {
      return;
    }

    Map<String, Figurita> figs = cargarFiguritas();
    cargarAdmin();
    Map<String, Perfil> perfs = cargarPerfiles(figs);
    cargarPropuestas(figs, perfs.get("lucas"), perfs.get("sofia"), perfs.get("juan"),perfs.get("valentina"));
    cargarSubastas(figs, perfs.get("lucas"), perfs.get("sofia"), perfs.get("matias"));
  }

//  private void limpiarBaseDeDatos() {
//    calificaciones.eliminarTodos();
//    propuestas.eliminarTodos();
//    subastas.eliminarTodos();
//    colecciones.eliminarTodos();
//    perfiles.eliminarTodos();
//    usuarios.eliminarTodos();
//    figuritas.eliminarTodos();
//  }

  // ─── FIGURITAS ────────────────────────────────────────────────────────────

  private Map<String, Figurita> cargarFiguritas() {
    List<Figurita> todas = FiguritasQatar2022.todas();
    todas.forEach(figuritas::guardar);
    return todas.stream().collect(Collectors.toMap(Figurita::getId, f -> f));
  }

  // ─── ADMIN ────────────────────────────────────────────────────────────────

  private void cargarAdmin() {
    Usuario admin = crearUsuario("admin", "admin", Rol.ADMINISTRADOR);
    Coleccion coleccion = crearColeccion();
    Perfil.builder()
        .usuario(admin)
        .nombre("admin")
        .coleccion(coleccion)
        .build();
  }

  // ─── PERFILES ─────────────────────────────────────────────────────────────

  private Map<String, Perfil> cargarPerfiles(Map<String, Figurita> figs) {
      // ── LUCAS ───────────────────────────────────────────────────────────────
//    Faltantes: Lewandowski (POL-9), Eriksen (DEN-10), Xhaka (SUI-10), Kimmich (GER-6), De Bruyne (BEL-7), Modrić (CRO-10), Ronaldo POR-7, Pedri "ESP-10"
//    Repetidas: Messi x3, Di María x2, J.Álvarez x2, E.Fernández x2, Neymar x2, Vinícius x3, Mbappé x2, Griezmann x2
    Coleccion coleccionLucas = crearColeccion();
    coleccionLucas.getFaltantes().addAll(List.of(
          figs.get("POL-9"),  // Lewandowski
          figs.get("DEN-10"), // Eriksen
          figs.get("SUI-10"), // Xhaka
          figs.get("GER-6"),  // Kimmich
          figs.get("BEL-7"),  // De Bruyne
          figs.get("CRO-10"),  // Modrić
          figs.get("POR-7"),  // Ronaldo
          figs.get("ESP-10")  // Pedri
      ));
      Usuario userLucas = crearUsuario("lucas_fis", "gordo123", Rol.USUARIO);
      Perfil lucas = crearPerfil("lucas-id-001", "Lucas", userLucas, coleccionLucas, "@lucas");

      agregarRepetidaNueva(coleccionLucas, figs.get("ARG-10"), 3, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), lucas.getId()); // Messi x3
      agregarRepetidaNueva(coleccionLucas, figs.get("ARG-11"), 2, List.of(MetodoIntercambio.INTERCAMBIO), lucas.getId());                            // Di María x2
      agregarRepetidaNueva(coleccionLucas, figs.get("ARG-9"),  2, List.of(MetodoIntercambio.INTERCAMBIO), lucas.getId());                            // J. Álvarez x2
      agregarRepetidaNueva(coleccionLucas, figs.get("ARG-8"),  2, List.of(MetodoIntercambio.SUBASTA),     lucas.getId());                            // E. Fernández x2
      agregarRepetidaNueva(coleccionLucas, figs.get("BRA-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), lucas.getId()); // Neymar x2
      agregarRepetidaNueva(coleccionLucas, figs.get("BRA-11"), 3, List.of(MetodoIntercambio.SUBASTA),     lucas.getId());                            // Vinícius x3
      agregarRepetidaNueva(coleccionLucas, figs.get("FRA-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), lucas.getId()); // Mbappé x2
      agregarRepetidaNueva(coleccionLucas, figs.get("FRA-7"),  2, List.of(MetodoIntercambio.INTERCAMBIO), lucas.getId());                            // Griezmann x2

      colecciones.guardar(coleccionLucas);

      // ── SOFÍA ───────────────────────────────────────────────────────────────
//    Faltantes: Messi (ARG-10), J.Álvarez (ARG-9), Neymar (BRA-10), Vinícius (BRA-11), D.Núñez (URU-9), E.Valencia (ECU-9),Griezmann, E.Fernández
//    Repetidas: Pedri x2, Morata x2, Kimmich x3, Neuer x2, Ronaldo x3, B.Fernandes x2,
    Coleccion coleccionSofia = crearColeccion();

    coleccionSofia.getFaltantes().addAll(List.of(
          figs.get("ARG-10"), // Messi
          figs.get("ARG-9"),  // J. Álvarez
          figs.get("BRA-10"), // Neymar
          figs.get("BRA-11"), // Vinícius
          figs.get("URU-9"),  // D. Núñez
          figs.get("ECU-9"),   // E. Valencia
          figs.get("FRA-7"), //Griezmann
          figs.get("ARG-8") //E. fernandez
      ));
      Usuario userSofia = crearUsuario("sofia_ape", "password", Rol.USUARIO);
      Perfil sofia = crearPerfil("sofia-id-001", "Sofía", userSofia, coleccionSofia, "@sofia");

      agregarRepetidaNueva(coleccionSofia, figs.get("ESP-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO), sofia.getId()); // Pedri x2
      agregarRepetidaNueva(coleccionSofia, figs.get("ESP-7"),  2, List.of(MetodoIntercambio.INTERCAMBIO), sofia.getId()); // Morata x2
      agregarRepetidaNueva(coleccionSofia, figs.get("GER-6"),  3, List.of(MetodoIntercambio.INTERCAMBIO), sofia.getId()); // Kimmich x3
      agregarRepetidaNueva(coleccionSofia, figs.get("GER-1"),  2, List.of(MetodoIntercambio.INTERCAMBIO), sofia.getId()); // Neuer x2
      agregarRepetidaNueva(coleccionSofia, figs.get("POR-7"),  3, List.of(MetodoIntercambio.INTERCAMBIO), sofia.getId()); // Ronaldo x3
      agregarRepetidaNueva(coleccionSofia, figs.get("POR-8"),  2, List.of(MetodoIntercambio.INTERCAMBIO), sofia.getId()); // B. Fernandes x2

      colecciones.guardar(coleccionSofia);

      // ── MATÍAS ──────────────────────────────────────────────────────────────
//    Faltantes: Pedri (ESP-10), Gavi (ESP-6), Mbappé (FRA-10), Griezmann (FRA-7), Ronaldo (POR-7), B.Silva (POR-10)
//    Repetidas: D.Núñez x2, De Arrascaeta x2, Valverde x2, E.Valencia x3, Caicedo x2
    Coleccion coleccionMatias = crearColeccion();

    coleccionMatias.getFaltantes().addAll(List.of(
          figs.get("ESP-10"), // Pedri
          figs.get("ESP-6"),  // Gavi
          figs.get("FRA-10"), // Mbappé
          figs.get("FRA-7"),  // Griezmann
          figs.get("POR-7"),  // Ronaldo
          figs.get("POR-10")  // B. Silva
      ));
      Usuario userMatias = crearUsuario("mati_crim", "wordpass", Rol.USUARIO);
      Perfil matias = crearPerfil("matias-id-001", "Matías", userMatias, coleccionMatias, "@matias");

      agregarRepetidaNueva(coleccionMatias, figs.get("URU-9"),  2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), matias.getId()); // D. Núñez x2
      agregarRepetidaNueva(coleccionMatias, figs.get("URU-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO), matias.getId());                            // De Arrascaeta x2
      agregarRepetidaNueva(coleccionMatias, figs.get("URU-8"),  2, List.of(MetodoIntercambio.SUBASTA),     matias.getId());                            // Valverde x2
      agregarRepetidaNueva(coleccionMatias, figs.get("ECU-9"),  3, List.of(MetodoIntercambio.INTERCAMBIO), matias.getId());                            // E. Valencia x3
      agregarRepetidaNueva(coleccionMatias, figs.get("ECU-8"),  2, List.of(MetodoIntercambio.INTERCAMBIO), matias.getId());                            // Caicedo x2

      colecciones.guardar(coleccionMatias);

      // ── JUAN ────────────────────────────────────────────────────────────────
//    Faltantes: Messi (ARG-10), Di María (ARG-11), J.Álvarez (ARG-9), E.Fernández (ARG-8)
//    Repetidas: Ziyech x2, Hakimi x2, Modrić x2
    Coleccion coleccionJuan = crearColeccion();

    coleccionJuan.getFaltantes().addAll(List.of(
          figs.get("ARG-10"), // Messi
          figs.get("ARG-11"), // Di María
          figs.get("ARG-9"),  // J. Álvarez
          figs.get("ARG-8")   // E. Fernández
      ));
      Usuario userJuan = crearUsuario("juan_jose", "una_contrasenia", Rol.USUARIO);
      Perfil juan = crearPerfil("juan-id-001", "Juan", userJuan, coleccionJuan, "@juan");

      agregarRepetidaNueva(coleccionJuan, figs.get("MAR-7"),  2, List.of(MetodoIntercambio.INTERCAMBIO), juan.getId()); // Ziyech x2
      agregarRepetidaNueva(coleccionJuan, figs.get("MAR-2"),  2, List.of(MetodoIntercambio.INTERCAMBIO), juan.getId()); // Hakimi x2
      agregarRepetidaNueva(coleccionJuan, figs.get("CRO-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO), juan.getId()); // Modrić x2

      colecciones.guardar(coleccionJuan);

      // ── VALENTINA ───────────────────────────────────────────────────────────
//    Faltantes: Messi (ARG-10), Di María (ARG-11), Neymar (BRA-10), Vinícius (BRA-11), Mbappé (FRA-10), Kane (ENG-9)
//    Repetidas: Ziyech x2, En-Nesyri x2, Mané x3, Koulibaly x2, Modrić x3, Perišić x2
    Coleccion coleccionValentina = crearColeccion();

    coleccionValentina.getFaltantes().addAll(List.of(
          figs.get("ARG-10"), // Messi
          figs.get("ARG-11"), // Di María
          figs.get("BRA-10"), // Neymar
          figs.get("BRA-11"), // Vinícius
          figs.get("FRA-10"), // Mbappé
          figs.get("ENG-9")   // Kane
      ));
      Usuario userValentina = crearUsuario("vale_gom", "passval", Rol.USUARIO);
      Perfil valentina = crearPerfil("valentina-id-001", "Valentina", userValentina, coleccionValentina, "@valentina");

      agregarRepetidaNueva(coleccionValentina, figs.get("MAR-7"),  2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), valentina.getId()); // Ziyech x2
      agregarRepetidaNueva(coleccionValentina, figs.get("MAR-9"),  2, List.of(MetodoIntercambio.SUBASTA),     valentina.getId());                            // En-Nesyri x2
      agregarRepetidaNueva(coleccionValentina, figs.get("SEN-10"), 3, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), valentina.getId()); // Mané x3
      agregarRepetidaNueva(coleccionValentina, figs.get("SEN-4"),  2, List.of(MetodoIntercambio.INTERCAMBIO), valentina.getId());                            // Koulibaly x2
      agregarRepetidaNueva(coleccionValentina, figs.get("CRO-10"), 3, List.of(MetodoIntercambio.SUBASTA),     valentina.getId());                            // Modrić x3
      agregarRepetidaNueva(coleccionValentina, figs.get("CRO-7"),  2, List.of(MetodoIntercambio.INTERCAMBIO), valentina.getId());                            // Perišić x2

      colecciones.guardar(coleccionValentina);

      // ── DIEGO ───────────────────────────────────────────────────────────────
//    Faltantes: D.Martínez (ARG-1), Bellingham (ENG-8), Van Dijk (NED-4), Kubo (JPN-7), Son (KOR-7), Partey (GHA-6)
//    Repetidas: Messi x2, J.Álvarez x2, Kane x2, Sterling x2, Depay x2, Gakpo x2, De Bruyne x3, Lukaku x2, Ronaldo x2
    Coleccion coleccionDiego = crearColeccion();

    coleccionDiego.getFaltantes().addAll(List.of(
          figs.get("ARG-1"),  // D. Martínez
          figs.get("ENG-8"),  // Bellingham
          figs.get("NED-4"),  // Van Dijk
          figs.get("JPN-7"),  // Kubo
          figs.get("KOR-7"),  // Son
          figs.get("GHA-6")   // Partey
      ));
      Usuario userDiego = crearUsuario("diego_ram", "diegopass", Rol.USUARIO);
      Perfil diego = crearPerfil("diego-id-001", "Diego", userDiego, coleccionDiego, "@diego");

      agregarRepetidaNueva(coleccionDiego, figs.get("ARG-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), diego.getId()); // Messi x2
      agregarRepetidaNueva(coleccionDiego, figs.get("ARG-9"),  2, List.of(MetodoIntercambio.SUBASTA),     diego.getId());                            // J. Álvarez x2
      agregarRepetidaNueva(coleccionDiego, figs.get("ENG-9"),  2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), diego.getId()); // Kane x2
      agregarRepetidaNueva(coleccionDiego, figs.get("ENG-10"), 2, List.of(MetodoIntercambio.INTERCAMBIO), diego.getId());                            // Sterling x2
      agregarRepetidaNueva(coleccionDiego, figs.get("NED-9"),  2, List.of(MetodoIntercambio.SUBASTA),     diego.getId());                            // Depay x2
      agregarRepetidaNueva(coleccionDiego, figs.get("NED-11"), 2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), diego.getId()); // Gakpo x2
      agregarRepetidaNueva(coleccionDiego, figs.get("BEL-7"),  3, List.of(MetodoIntercambio.SUBASTA),     diego.getId());                            // De Bruyne x3
      agregarRepetidaNueva(coleccionDiego, figs.get("BEL-9"),  2, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA), diego.getId()); // Lukaku x2
      agregarRepetidaNueva(coleccionDiego, figs.get("POR-7"),  2, List.of(MetodoIntercambio.SUBASTA),     diego.getId());                            // Ronaldo x2

      colecciones.guardar(coleccionDiego);

    return Map.of(
        "lucas", lucas,
        "sofia", sofia,
        "matias", matias,
        "juan", juan,
        "valentina", valentina,
        "diego", diego
    );
  }
// ─── PROPUESTAS ───────────────────────────────────────────────────────────
//  P01 — Lucas → Juan: ofrece Messi → busca Modrić
//  P02 — Lucas → Juan: ofrece Di María → busca Modrić
//  P03 — Lucas → Valentina: ofrece Neymar → busca Modrić
//  P04 — Lucas → Valentina: ofrece Vinícius → busca Modrić
//  P05 — Lucas → Sofía: ofrece Griezmann → busca Kimmich
//  P06 — Lucas → Sofía: ofrece J.Álvarez → busca Kimmich
//  P07 — Valentina → Lucas: ofrece Modrić → busca Vinícius
//  P08 — Sofía → Lucas: ofrece Kimmich → busca Messi
private void cargarPropuestas(Map<String, Figurita> figs,
                              Perfil lucas, Perfil sofia,
                              Perfil juan, Perfil valentina) {

  /// ── PROPUESTAS ───────────────────────────────────────────────────────────

// P01 — Lucas → Juan : ofrece Messi, busca Modrić
  propuestas.guardar(Propuesta.builder()
      .autor(lucas)
      .destinatario(juan)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("ARG-10"))))
      .figuritaBuscada(figs.get("CRO-10"))
      .build());
  reservar(lucas, "ARG-10");

// P02 — Lucas → Juan : ofrece Di María, busca Modrić
  propuestas.guardar(Propuesta.builder()
      .autor(lucas)
      .destinatario(juan)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("ARG-11"))))
      .figuritaBuscada(figs.get("CRO-10"))
      .build());
  reservar(lucas, "ARG-11");

// P03 — Lucas → Valentina : ofrece Neymar, busca Modrić
  propuestas.guardar(Propuesta.builder()
      .autor(lucas)
      .destinatario(valentina)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("BRA-10"))))
      .figuritaBuscada(figs.get("CRO-10"))
      .build());
  reservar(lucas, "BRA-10");

// P04 — Lucas → Valentina : ofrece Vinícius, busca Modrić
  propuestas.guardar(Propuesta.builder()
      .autor(lucas)
      .destinatario(valentina)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("BRA-11"))))
      .figuritaBuscada(figs.get("CRO-10"))
      .build());
  reservar(lucas, "BRA-11");

// P05 — Lucas → Sofía : ofrece Griezmann, busca Kimmich
  propuestas.guardar(Propuesta.builder()
      .autor(lucas)
      .destinatario(sofia)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("FRA-7"))))
      .figuritaBuscada(figs.get("GER-6"))
      .build());
  reservar(lucas, "FRA-7");

// P06 — Lucas → Sofía : ofrece J.Álvarez, busca Kimmich
  propuestas.guardar(Propuesta.builder()
      .autor(lucas)
      .destinatario(sofia)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("ARG-9"))))
      .figuritaBuscada(figs.get("GER-6"))
      .build());
  reservar(lucas, "ARG-9");

// P07 — Valentina → Lucas : ofrece Modrić, busca Vinícius
  propuestas.guardar(Propuesta.builder()
      .autor(valentina)
      .destinatario(lucas)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("CRO-10"))))
      .figuritaBuscada(figs.get("BRA-11"))
      .build());
  reservar(valentina, "CRO-10");

// P08 — Sofía → Lucas : ofrece Kimmich, busca Messi
  propuestas.guardar(Propuesta.builder()
      .autor(sofia)
      .destinatario(lucas)
      .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("GER-6"))))
      .figuritaBuscada(figs.get("ARG-10"))
      .build());
  reservar(sofia, "GER-6");
}

// Busca la FiguritaIntercambiable en las repetidas del perfil por id de figurita
// y le incrementa cantidadReservada en 1.
private void reservar(Perfil perfil, String figuritaId) {
  perfil.getColeccion().getRepetidas().stream()
      .filter(fi -> fi.getFigurita().getId().equals(figuritaId))
      .findFirst()
      .ifPresent(fi -> fi.setCantidadReservada(fi.getCantidadReservada() + 1));

  colecciones.guardar(perfil.getColeccion());
}

  // ─── SUBASTAS ─────────────────────────────────────────────────────────────
//  S1 — Lucas subasta Griezmann
//
//  Oferta de Matías: ofrece D.Núñez
//  Oferta de Sofía: ofrece Pedri
//
//
//  S2 — Sofía subasta Ronaldo
//
//  Oferta de Lucas: ofrece E.Fernández → reserva(lucas, ARG-8)
  private void cargarSubastas(Map<String, Figurita> figs,
                              Perfil lucas, Perfil sofia, Perfil matias) {
// S1 — Lucas subasta Griezmann
    Subasta s1 = Subasta.builder()
        .autor(lucas)
        .figuritaSubastada(figs.get("FRA-7"))
        .fechaInicio(LocalDateTime.now().minusDays(1))
        .fechaCierre(LocalDateTime.now().plusDays(7))
        .build();
    reservar(lucas, "FRA-7");
    subastas.guardar(s1);

// Oferta de Matías a S1
    Propuesta ofertaMatias = Propuesta.builder()
        .id("1")
        .autor(matias)
        .destinatario(lucas)
        .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("URU-9"))))
        .figuritaBuscada(figs.get("FRA-7"))
        .build();
    s1.getOfertas().add(ofertaMatias);
    reservar(matias, "URU-9");

// Oferta de Sofía a S1
    Propuesta ofertaSofia = Propuesta.builder()
        .id("2")
        .autor(sofia)
        .destinatario(lucas)
        .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("ESP-10"))))
        .figuritaBuscada(figs.get("FRA-7"))
        .build();
    s1.getOfertas().add(ofertaSofia);
    reservar(sofia, "ESP-10");

    subastas.guardar(s1);

// S2 — Sofía subasta Ronaldo
    Subasta s2 = Subasta.builder()
        .autor(sofia)
        .figuritaSubastada(figs.get("POR-7"))
        .fechaInicio(LocalDateTime.now().minusDays(1))
        .fechaCierre(LocalDateTime.now().plusDays(7))
        .build();
    reservar(sofia, "POR-7");
    subastas.guardar(s2);

// Oferta de Lucas a S2
    Propuesta ofertaLucas = Propuesta.builder()
        .id("3")
        .autor(lucas)
        .destinatario(sofia)
        .figuritasOfrecidas(new ArrayList<>(List.of(figs.get("ARG-8"))))
        .figuritaBuscada(figs.get("POR-7"))
        .build();
    s2.getOfertas().add(ofertaLucas);
    reservar(lucas, "ARG-8");

    subastas.guardar(s2);
  }

}