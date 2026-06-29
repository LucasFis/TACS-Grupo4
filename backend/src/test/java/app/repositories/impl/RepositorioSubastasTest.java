package app.repositories.impl;

import app.MongoTestBase;
import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.*;
import app.repositories.impl.campos.CamposSubasta;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioSubastasTest extends MongoTestBase {

    private Perfil p1;
    private Perfil p2;
    private Perfil participante;

    private List<MedioDeContacto> telegram(String numero) {
        return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
    }

    @BeforeEach
    void setUp() {

        Usuario user = new Usuario("u-1000", Rol.USUARIO, "lucas", "fiscella");
        Coleccion colec = new Coleccion("c-1000");

        repositorioColecciones.guardar(colec);
        repositorioUsuarios.guardar(user);

        p1 = Perfil.builder()
            .id(new ObjectId().toString())
            .usuario(user)
            .nombre("Lucas")
            .coleccion(colec)
            .mediosDeContacto(telegram("@lucas"))
            .build();

        repositorioPerfiles.guardar(p1);

        user = new Usuario("u-1001", Rol.USUARIO, "sofia", "fiscella");
        colec = new Coleccion("c-1001");

        repositorioColecciones.guardar(colec);
        repositorioUsuarios.guardar(user);

        p2 = Perfil.builder()
            .id(new ObjectId().toString())
            .usuario(user)
            .nombre("Sofia")
            .coleccion(colec)
            .mediosDeContacto(telegram("@sofia"))
            .build();

        repositorioPerfiles.guardar(p2);

        user = new Usuario("u-1002", Rol.USUARIO, "ana", "fiscella");
        colec = new Coleccion("c-1002");

        repositorioColecciones.guardar(colec);
        repositorioUsuarios.guardar(user);

        participante = Perfil.builder()
            .id(new ObjectId().toString())
            .usuario(user)
            .nombre("Ana")
            .coleccion(colec)
            .mediosDeContacto(telegram("@ana"))
            .build();

        repositorioPerfiles.guardar(participante);
    }

    @Test
    void findByUsuarioId_retornaSoloSubastasDelUsuario() {
        Subasta s1 = crearSubasta(
            "s-1",
            p1,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(1)
        );

        Subasta s2 = crearSubasta(
            "s-2",
            p2,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(1)
        );

        repositorioSubastas.guardar(s1);
        repositorioSubastas.guardar(s2);

        PaginaResultado<Subasta> resultado = repositorioSubastas.buscarPorAutor(p1.getId(), 0, 10, new CamposSubasta(true, true));

        assertEquals(1, resultado.contenido().size());
        assertEquals("s-1", resultado.contenido().get(0).getId());
    }

    @Test
    void findByUsuarioId_sinResultados_retornaListaVacia() {
        assertTrue(repositorioSubastas.buscarPorAutor(new ObjectId().toString(), 1, 10, new CamposSubasta(true, true)).contenido().isEmpty());
        assertTrue(
            repositorioSubastas
                .buscarPorAutor(new ObjectId().toString(), 1, 10, new CamposSubasta(true, true))
                .contenido()
                .isEmpty()
        );
    }

    @Test
    void buscarTodos_sinFiltros_retornaTodasLasSubastas() {

        repositorioSubastas.guardar(
            crearSubasta(
                "s-1",
                p1,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(1)
            )
        );

        repositorioSubastas.guardar(
            crearSubasta(
                "s-2",
                p2,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(1)
            )
        );

        SubastasFiltro filtros =
            new SubastasFiltro(1,10,null,null,null, null, null);

        PaginaResultado<Subasta> resultado =
            repositorioSubastas.buscarTodos(filtros, new CamposSubasta(true, true));

        assertEquals(2, resultado.cantidadDeElementos());
        assertEquals(2, resultado.contenido().size());
    }

    @Test
    void buscarTodos_filtraPorAutor() {

        repositorioSubastas.guardar(
            crearSubasta(
                "s-1",
                p1,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(1)
            )
        );

        repositorioSubastas.guardar(
            crearSubasta(
                "s-2",
                p2,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(1)
            )
        );

        SubastasFiltro filtros =
            new SubastasFiltro(
                1,
                10,
                p1.getId(),
                null,
                null,
                null,
                null
            );

        PaginaResultado<Subasta> resultado =
            repositorioSubastas.buscarTodos(filtros, new CamposSubasta(true, true));

        assertEquals(1, resultado.contenido().size());
        assertEquals(
            p1.getId(),
            resultado.contenido().get(0).getAutor().getId()
        );
    }

    @Test
    void buscarTodos_filtraSubastasActivas() {

        repositorioSubastas.guardar(
            crearSubasta(
                "activa",
                p1,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2)
            )
        );

        repositorioSubastas.guardar(
            crearSubasta(
                "finalizada",
                p1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1)
            )
        );

        SubastasFiltro filtros =
            new SubastasFiltro(
                1,
                10,
                null,
                null,
                "ACTIVA",
                null,
                null
            );

        PaginaResultado<Subasta> resultado =
            repositorioSubastas.buscarTodos(filtros, new CamposSubasta(true, true));

        assertEquals(1, resultado.contenido().size());
        assertEquals(
            "activa",
            resultado.contenido().get(0).getId()
        );
    }

    @Test
    void buscarTodos_filtraSubastasFinalizadas() {

        repositorioSubastas.guardar(
            crearSubasta(
                "activa",
                p1,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2)
            )
        );

        repositorioSubastas.guardar(
            crearSubasta(
                "finalizada",
                p1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1)
            )
        );

        SubastasFiltro filtros =
            new SubastasFiltro(
                1,
                10,
                null,
                null,
                "FINALIZADA",
                null,
                null
            );

        PaginaResultado<Subasta> resultado =
            repositorioSubastas.buscarTodos(filtros, new CamposSubasta(true, true));

        assertEquals(1, resultado.contenido().size());
        assertEquals(
            "finalizada",
            resultado.contenido().get(0).getId()
        );
    }

    @Test
    void buscarTodos_filtraPorParticipante() {

        Propuesta propuesta = Propuesta.builder()
            .autor(participante)
            .figuritasOfrecidas(List.of())
            .build();

        Subasta s1 = crearSubasta(
            "s-1",
            p1,
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().plusHours(2)
        );

        s1.setOfertas(List.of(propuesta));

        repositorioSubastas.guardar(s1);

        repositorioSubastas.guardar(
            crearSubasta(
                "s-2",
                p2,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2)
            )
        );

        SubastasFiltro filtros =
            new SubastasFiltro(
                1,
                10,
                null,
                participante.getId(),
                null,
                null,
                null
            );

        PaginaResultado<Subasta> resultado =
            repositorioSubastas.buscarTodos(filtros, new CamposSubasta(true, true));

        assertEquals(1, resultado.contenido().size());
        assertEquals(
            "s-1",
            resultado.contenido().get(0).getId()
        );
    }

    @Test
    void buscarTodos_aplicaPaginacion() {

        repositorioSubastas.guardar(
            crearSubasta(
                "s-1",
                p1,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
            )
        );

        repositorioSubastas.guardar(
            crearSubasta(
                "s-2",
                p1,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
            )
        );

        repositorioSubastas.guardar(
            crearSubasta(
                "s-3",
                p1,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
            )
        );

        SubastasFiltro filtros =
            new SubastasFiltro(
                2,
                2,
                null,
                null,
                null,
                null,
                null
            );

        PaginaResultado<Subasta> resultado =
            repositorioSubastas.buscarTodos(filtros, new CamposSubasta(true, true));

        assertEquals(3, resultado.cantidadDeElementos());
        assertEquals(2, resultado.cantidadDePaginas());
        assertEquals(2, resultado.numero());
        assertEquals(1, resultado.contenido().size());
    }

    private Subasta crearSubasta(
        String id,
        Perfil autor,
        LocalDateTime inicio,
        LocalDateTime cierre
    ) {
        return Subasta.builder()
            .id(id)
            .autor(autor)
            .fechaInicio(inicio)
            .fechaCierre(cierre)
            .build();
    }
}