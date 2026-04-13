package app.controllers;

import app.dto.OperacionesDto;
import app.dto.TemporalDto;
import app.model.entities.Sugerencia;
import app.model.entities.Usuario;
import app.repositories.RepositorioUsuarios;
import app.servicios.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RepositorioUsuarios usuarioRepositorio;

    public UsuarioController(UsuarioService usuarioService, RepositorioUsuarios usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{user_id}/operaciones")
    public ResponseEntity<OperacionesDto> getOperaciones(@PathVariable String user_id) {
        OperacionesDto operaciones = usuarioService.getOperacionesUsuario(user_id);
        if (operaciones == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(operaciones);
    }

    @PostMapping("/{user_id}/calificaciones")
    public ResponseEntity<TemporalDto> calificarUsuario(@PathVariable String user_id, @RequestBody Map<String, Object> body) {

        try {
            Usuario usuario = this.usuarioRepositorio.findById(user_id);
            Integer calificacion = (Integer) body.get("calificacion");

            usuario.getCalificaciones().add(calificacion);

            this.usuarioRepositorio.save(usuario);


            return ResponseEntity.ok(new TemporalDto("Nueva calificacion: " + usuario.getCalificacionMedia()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new TemporalDto("Bad request: " + e.getMessage()));
        }
    }

    @GetMapping("/{user_id}/sugerencias")
    public ResponseEntity<TemporalDto> getSugerencias(@PathVariable String user_id) {
        Usuario usuarioObjetivo = this.usuarioRepositorio.findById(user_id);
        List<Usuario> usuarios = this.usuarioRepositorio.findAll();
        List<Sugerencia> sugerencias = new ArrayList<>();

        usuarios.forEach(usuario -> {
            Sugerencia sugerencia = new Sugerencia(usuario, new ArrayList<>());

            usuario.getColeccion().getRepetidas().forEach(repetida -> {
                if(usuarioObjetivo.getColeccion().getFaltantes().contains(repetida.getFigurita())){
                    sugerencia.getFiguritasSugeridas().add(repetida.getFigurita());
                }
            });

            if(!sugerencia.getFiguritasSugeridas().isEmpty()){
                sugerencias.add(sugerencia);
            }
        });

        sugerencias.forEach(sugerencia -> {
            System.out.println("Usuario: " + sugerencia.getUsuarioSugerido().getNombre());
            sugerencia.getFiguritasSugeridas().forEach(figurita -> {
                System.out.println("Jugador: " + figurita.getJugador());
            });
        });

        return ResponseEntity.ok(new TemporalDto("Sugerencias totales: " + sugerencias.size()));
    }
}
