package app.repositories.impl;

import app.MongoTestBase;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.*;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioPropuestasTest extends MongoTestBase {

    @Autowired
    private RepositorioPropuestasMongo repositorio;

    private Perfil u1;
    private Perfil u2;
    private Perfil u3;

    private List<MedioDeContacto> telegram(String usuario) {
        return List.of(
            new MedioDeContacto(
                MedioComunicacion.TELEGRAM,
                usuario
            )
        );
    }

    @BeforeEach
    void setUp() {

        mongoTemplate.dropCollection(Propuesta.class);

        u1 = Perfil.builder()
            .id(new ObjectId().toString())
            .usuario(new Usuario("u1", Rol.USUARIO, "lucas", "123"))
            .nombre("Lucas")
            .mediosDeContacto(telegram("@lucas"))
            .build();

        u2 = Perfil.builder()
            .id(new ObjectId().toString())
            .usuario(new Usuario("u2", Rol.USUARIO, "sofia", "123"))
            .nombre("Sofia")
            .mediosDeContacto(telegram("@sofia"))
            .build();

        u3 = Perfil.builder()
            .id(new ObjectId().toString())
            .usuario(new Usuario("u3", Rol.USUARIO, "mati", "123"))
            .nombre("Matias")
            .mediosDeContacto(telegram("@mati"))
            .build();
    }

    private Propuesta propuesta(
        Perfil autor,
        Perfil destinatario,
        EstadoProceso... estados
    ) {

        Propuesta propuesta = Propuesta.builder()
            .autor(autor)
            .destinatario(destinatario)
            .figuritasOfrecidas(List.of())
            .build();

        List<EstadoPropuesta> historial = new ArrayList<>();

        for (EstadoProceso estado : estados) {
            historial.add(
                new EstadoPropuesta(
                    LocalDateTime.now(),
                    estado
                )
            );
        }

        propuesta.setEstado(historial);

        // sincroniza estadoActual con el último estado
        propuesta.setEstadoActual(
            historial.get(historial.size() - 1)
        );

        return propuesta;
    }

    @Test
    void guardar_guardaCorrectamente() {

        Propuesta propuesta =
            propuesta(u1, u2, EstadoProceso.PENDIENTE);

        repositorio.guardar(propuesta);

        assertEquals(
            1,
            repositorio.contar()
        );
    }

    @Test
    void buscarPorId_existente_devuelvePropuesta() {

        Propuesta propuesta =
            propuesta(u1, u2, EstadoProceso.PENDIENTE);

        repositorio.guardar(propuesta);

        Propuesta resultado =
            repositorio.buscarPorId(propuesta.getId());

        assertEquals(
            propuesta.getId(),
            resultado.getId()
        );
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {

        assertThrows(
            NotFoundException.class,
            () -> repositorio.buscarPorId(
                new ObjectId().toString()
            )
        );
    }

    @Test
    void contar_sinElementos_devuelveCero() {

        assertEquals(
            0,
            repositorio.contar()
        );
    }

    @Test
    void contar_conElementos_devuelveCantidad() {

        repositorio.guardar(
            propuesta(u1, u2, EstadoProceso.PENDIENTE)
        );

        repositorio.guardar(
            propuesta(u2, u1, EstadoProceso.PENDIENTE)
        );

        assertEquals(
            2,
            repositorio.contar()
        );
    }

    @Test
    void buscarTodos_recibidas_filtraCorrectamente() {

        repositorio.guardar(
            propuesta(u1, u2, EstadoProceso.PENDIENTE)
        );

        repositorio.guardar(
            propuesta(u1, u3, EstadoProceso.PENDIENTE)
        );

        PropuestasFiltro filtros =
            new PropuestasFiltro(
                "RECIBIDAS",
                0,
                10,
                null
            );

        PaginaResultado<Propuesta> resultado =
            repositorio.buscarTodos(
                u2.getId(),
                filtros
            );

        assertEquals(
            1,
            resultado.contenido().size()
        );
    }

    @Test
    void buscarTodos_enviadas_filtraCorrectamente() {

        repositorio.guardar(
            propuesta(u1, u2, EstadoProceso.PENDIENTE)
        );

        repositorio.guardar(
            propuesta(u2, u3, EstadoProceso.PENDIENTE)
        );

        PropuestasFiltro filtros =
            new PropuestasFiltro(
                "ENVIADAS",
                0,
                10,
                null
            );

        PaginaResultado<Propuesta> resultado =
            repositorio.buscarTodos(
                u1.getId(),
                filtros
            );

        assertEquals(
            1,
            resultado.contenido().size()
        );
    }

    @Test
    void buscarTodos_estadoFiltraPorEstadoActual() {

        repositorio.guardar(
            propuesta(
                u1,
                u2,
                EstadoProceso.PENDIENTE,
                EstadoProceso.RECHAZADO
            )
        );

        repositorio.guardar(
            propuesta(
                u1,
                u3,
                EstadoProceso.PENDIENTE
            )
        );

        PropuestasFiltro filtros =
            new PropuestasFiltro(
                "",
                0,
                10,
                EstadoProceso.RECHAZADO
            );

        PaginaResultado<Propuesta> resultado =
            repositorio.buscarTodos(
                u1.getId(),
                filtros
            );

        assertEquals(
            1,
            resultado.contenido().size()
        );

        assertEquals(
            EstadoProceso.RECHAZADO,
            resultado.contenido()
                .get(0)
                .getEstadoActual()
                .getValor()
        );
    }

    @Test
    void buscarEstadisticasPorRango_incluyeDentroYExcluyeFuera() {
        LocalDateTime dentroDelRango = LocalDateTime.now().minusDays(3);
        LocalDateTime fueraDelRango = LocalDateTime.now().minusDays(10);

        EstadoPropuesta estadoDentro = new EstadoPropuesta(dentroDelRango, EstadoProceso.PENDIENTE);
        Propuesta dentro = Propuesta.builder()
            .autor(u1).destinatario(u2).figuritasOfrecidas(List.of())
            .estado(new ArrayList<>(List.of(estadoDentro))).estadoActual(estadoDentro)
            .build();

        EstadoPropuesta estadoFuera = new EstadoPropuesta(fueraDelRango, EstadoProceso.PENDIENTE);
        Propuesta fuera = Propuesta.builder()
            .autor(u2).destinatario(u3).figuritasOfrecidas(List.of())
            .estado(new ArrayList<>(List.of(estadoFuera))).estadoActual(estadoFuera)
            .build();

        repositorio.guardar(dentro);
        repositorio.guardar(fuera);

        List<Propuesta> resultado = repositorio.buscarEstadisticasPorRango(
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now()
        );

        assertEquals(1, resultado.size());
        assertEquals(dentro.getId(), resultado.get(0).getId());
    }

    @Test
    void buscarTodos_sinResultados_devuelveListaVacia() {

        PropuestasFiltro filtros =
            new PropuestasFiltro(
                "",
                0,
                10,
                null
            );

        PaginaResultado<Propuesta> resultado =
            repositorio.buscarTodos(
                u1.getId(),
                filtros
            );

        assertTrue(
            resultado.contenido().isEmpty()
        );
    }
}