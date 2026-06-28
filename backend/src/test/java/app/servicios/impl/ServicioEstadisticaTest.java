package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.MongoTestBase;
import app.dto.EstadisticasDto;
import app.dto.SesionDto;
import app.exceptions.UnauthorizedException;
import app.model.entities.*;

import java.time.LocalDate;
import app.servicios.ServicioEstadisticas;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ServicioEstadisticaTest extends MongoTestBase {
    @Autowired
    private ServicioEstadisticas service;

    private List<MedioDeContacto> telegram(String numero) {
        return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
    }

    private SesionDto adminSesion() {
        return new SesionDto("user-id", "ADMINISTRADOR", "p-10", "c-10");
    }

    private Perfil perfil(String id, String usuarioId, String nombre) {
        Usuario user = new Usuario(usuarioId, Rol.USUARIO, "lucas", "fiscella");
        Coleccion colecccion = new Coleccion("c-"+ id);
        Perfil perfil = Perfil.builder()
            .id(id).usuario(user).nombre("nombre")
            .coleccion(colecccion)
            .mediosDeContacto(telegram("@" + nombre.toLowerCase()))
            .build();
        repositorioColecciones.guardar(colecccion);
        repositorioUsuarios.guardar(user);
        repositorioPerfiles.guardar(perfil);
        return perfil;
    }

    private Figurita figurita(String id, Seleccion seleccion) {
        Figurita fig = Figurita.builder()
            .id(id)
            .numero(1)
            .jugador("Jugador")
            .seleccion(seleccion)
            .posicion("Delantero")
            .build();
        repositorioFiguritas.guardar(fig);

        return fig;
    }

    private FiguritaIntercambiable soloIntercambio(Figurita f) {
        return new FiguritaIntercambiable(f, 1, List.of(MetodoIntercambio.INTERCAMBIO));
    }

    private FiguritaIntercambiable soloSubasta(Figurita f) {
        return new FiguritaIntercambiable(f, 1, List.of(MetodoIntercambio.SUBASTA));
    }

    private FiguritaIntercambiable ambosMetodos(Figurita f) {
        return new FiguritaIntercambiable(f, 1, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA));
    }

    @Test
    void getEstadisticas_sinDatos_retornaTodosCeros() {

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(0, resultado.getTotalUsuarios());
        assertEquals(0, resultado.getTotalFiguritasPublicadas());
        assertEquals(0, resultado.getTotalPropuestas());
        assertEquals(0, resultado.getTotalSubastasActivas());
    }

    @Test
    void getEstadisticas_conDatos_retornaValoresCorrectos() {
        Figurita f1 = figurita("f1", Seleccion.ARGENTINA);
        Figurita f2 = figurita("f2", Seleccion.BRASIL);
        Figurita f3 = figurita("f3", Seleccion.ARGENTINA);

        Coleccion coleccionConDos = new Coleccion();
        coleccionConDos.getRepetidas().add(soloIntercambio(f1));
        coleccionConDos.getRepetidas().add(soloIntercambio(f2));

        Coleccion coleccionConUna = new Coleccion();
        coleccionConUna.getRepetidas().add(soloIntercambio(f3));

        repositorioColecciones.guardar(coleccionConDos);
        repositorioColecciones.guardar(coleccionConUna);

        Usuario user =  new Usuario("usr-1", Rol.USUARIO, "lucas", "fiscella");
        repositorioUsuarios.guardar(user);

        Perfil u1 = Perfil.builder()
            .id("u-1").usuario(user).nombre("Lucas")
            .mediosDeContacto(telegram("@lucas"))
            .coleccion(coleccionConDos)
            .build();

        repositorioPerfiles.guardar(u1);

        user = new Usuario("usr-2", Rol.USUARIO, "lucas", "fiscella");
        repositorioUsuarios.guardar(user);
        Perfil u2 = Perfil.builder()
            .id("u-2").usuario(user).nombre("Sofía")
            .mediosDeContacto(telegram("@sofia"))
            .coleccion(coleccionConUna)
            .build();

        repositorioPerfiles.guardar(u2);

        Subasta subastaActiva = Subasta.builder().id("s-1").autor(u1).fechaInicio(
                LocalDateTime.now().minusHours(1)).fechaCierre(LocalDateTime.now().plusDays(1))
            .build();

        repositorioSubastas.guardar(subastaActiva);

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(2, resultado.getTotalUsuarios());
        assertEquals(3, resultado.getTotalFiguritasPublicadas());
        assertEquals(0, resultado.getTotalPropuestas());
        assertEquals(1, resultado.getTotalSubastasActivas());
    }

    @Test
    void getEstadisticas_filtraSoloSubastasActivas() {
        Perfil u1 = perfil("u-1", "usr-1", "Lucas");

        Subasta activa  = Subasta.builder().id("s-1").autor(u1).fechaInicio(
                LocalDateTime.now().minusHours(1)).fechaCierre(LocalDateTime.now().plusDays(1))
            .build();

        Subasta vencida = Subasta.builder().id("s-2").autor(u1).fechaInicio(
                LocalDateTime.now().minusDays(3)).fechaCierre(LocalDateTime.now().minusDays(1))
            .build();

        repositorioSubastas.guardar(activa);
        repositorioSubastas.guardar(vencida);

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(1, resultado.getTotalSubastasActivas());
    }

    @Test
    void propuestasPorEstado_sinPropuestas_retornaCeros() {

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(0, resultado.getPropuestasPorEstado().getPendientes());
        assertEquals(0, resultado.getPropuestasPorEstado().getAceptadas());
        assertEquals(0, resultado.getPropuestasPorEstado().getRechazadas());
    }

    @Test
    void propuestasPorEstado_conPropuestasMixtas_retornaConteoCorrecto() {
        Perfil autor = perfil("a", "usr-a", "Autor");
        Perfil destinatario = perfil("d", "usr-d", "Dest");

        Propuesta pendiente =
            propuestaConEstado(
                "p1",
                autor,
                destinatario,
                EstadoProceso.PENDIENTE
            );

        Propuesta aceptada =
            propuestaConEstado(
                "p2",
                autor,
                destinatario,
                EstadoProceso.ACEPTADO
            );

        Propuesta rechazada =
            propuestaConEstado(
                "p3",
                autor,
                destinatario,
                EstadoProceso.RECHAZADO
            );

        repositorioPropuestas.guardar(pendiente);
        repositorioPropuestas.guardar(aceptada);
        repositorioPropuestas.guardar(rechazada);

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(1, resultado.getPropuestasPorEstado().getPendientes());
        assertEquals(1, resultado.getPropuestasPorEstado().getAceptadas());
        assertEquals(1, resultado.getPropuestasPorEstado().getRechazadas());
    }

    @Test
    void propuestasPorEstado_variasDelMismoEstado_acumulaCorrectamente() {
        Perfil autor = perfil("a", "usr-a", "Autor");
        Perfil destinatario = perfil("d", "usr-d", "Dest");

        Propuesta pendiente =
            propuestaConEstado(
                "p1",
                autor,
                destinatario,
                EstadoProceso.PENDIENTE
            );

        Propuesta pendiente2 =
            propuestaConEstado(
                "p3",
                autor,
                destinatario,
                EstadoProceso.PENDIENTE
            );

        Propuesta aceptada =
            propuestaConEstado(
                "p2",
                autor,
                destinatario,
                EstadoProceso.ACEPTADO
            );


        repositorioPropuestas.guardar(pendiente);
        repositorioPropuestas.guardar(pendiente2);
        repositorioPropuestas.guardar(aceptada);

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(2, resultado.getPropuestasPorEstado().getPendientes());
        assertEquals(1, resultado.getPropuestasPorEstado().getAceptadas());
        assertEquals(0, resultado.getPropuestasPorEstado().getRechazadas());
    }

    @Test
    void figuritasPorModalidad_sinFiguritas_retornaCeros() {

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(0, resultado.getFiguritasPorModalidad().getSoloIntercambio());
        assertEquals(0, resultado.getFiguritasPorModalidad().getSoloSubasta());
        assertEquals(0, resultado.getFiguritasPorModalidad().getAmbos());
    }

    @Test
    void figuritasPorModalidad_conFiguritasMixtas_retornaConteoCorrecto() {
        Figurita f1 = figurita("f1", Seleccion.ARGENTINA);
        Figurita f2 = figurita("f2", Seleccion.BRASIL);
        Figurita f3 = figurita("f3", Seleccion.ESPAÑA);

        Coleccion coleccion = new Coleccion();
        coleccion.getRepetidas().add(soloIntercambio(f1));
        coleccion.getRepetidas().add(soloSubasta(f2));
        coleccion.getRepetidas().add(ambosMetodos(f3));

        repositorioColecciones.guardar(coleccion);

        Usuario user = new Usuario("usr-1", Rol.USUARIO, "lucas", "fiscella");

        repositorioUsuarios.guardar(user);

        Perfil u1 = Perfil.builder()
            .id("1").usuario(user)
            .nombre("Lucas").coleccion(coleccion)
            .mediosDeContacto(telegram("@lucas"))
            .build();

        repositorioPerfiles.guardar(u1);

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(1, resultado.getFiguritasPorModalidad().getSoloIntercambio());
        assertEquals(1, resultado.getFiguritasPorModalidad().getSoloSubasta());
        assertEquals(1, resultado.getFiguritasPorModalidad().getAmbos());
    }

    @Test
    void obtenerEstadisticas_usuarioNoAdmin_lanzaUnauthorized() {
        SesionDto sesion = new SesionDto("u1", "USUARIO", "p-11", "c-10");

        assertThrows(
            UnauthorizedException.class,
            () -> service.obtenerEstadisticas(sesion, LocalDate.now().minusDays(7), LocalDate.now())
        );
    }

    @Test
    void propuestasPorEstado_incluyeCanceladas() {
        Perfil autor = perfil("a", "usr-a", "Autor");
        Perfil destinatario = perfil("d", "usr-d", "Dest");

        repositorioPropuestas.guardar(propuestaConEstado("p1", autor, destinatario, EstadoProceso.CANCELADO));

        EstadisticasDto resultado = service.obtenerEstadisticas(adminSesion(), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(1, resultado.getPropuestasPorEstado().getCanceladas());
        assertEquals(0, resultado.getPropuestasPorEstado().getPendientes());
    }

    private Propuesta propuestaConEstado(
        String id,
        Perfil autor,
        Perfil destinatario,
        EstadoProceso estado
    ) {
        EstadoPropuesta estadoActual = new EstadoPropuesta(
            LocalDateTime.now(),
            estado
        );

        return Propuesta.builder()
            .id(id)
            .autor(autor)
            .destinatario(destinatario)
            .figuritasOfrecidas(List.of())
            .figuritaBuscada(null)
            .estado(new ArrayList<>(List.of(estadoActual)))
            .estadoActual(estadoActual)
            .build();
    }
}