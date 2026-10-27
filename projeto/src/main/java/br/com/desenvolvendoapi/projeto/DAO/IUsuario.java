package br.com.desenvolvendoapi.projeto.DAO;

import br.com.desenvolvendoapi.projeto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUsuario extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByEmail(String email);
}
