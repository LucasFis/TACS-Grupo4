package app.repositories;

import app.model.entities.Figurita;
import app.model.entities.Usuario;
import java.util.List;

import java.util.List;

public interface RepositorioUsuarios {

    Usuario buscarPorId(String id);

    List<Usuario> buscarTodos();

    int contar();

    List<Usuario> buscarPorFiguritaFaltante(Figurita figurita);

    void guardar(Usuario usuario);
}
