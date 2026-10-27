package br.com.desenvolvendoapi.projeto.service;

import br.com.desenvolvendoapi.projeto.DAO.IUsuario;
import br.com.desenvolvendoapi.projeto.model.Usuario;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {
    private IUsuario DAO;
    private PasswordEncoder passwordEncoder;

    public UsuarioService(IUsuario DAO) {
        this.DAO = DAO;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<Usuario> listarUsuario() {
        List<Usuario> lista = DAO.findAll();
        return lista;
    }

    public Usuario criarUsuario(Usuario usuario){
        String encoder = this.passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(encoder);
        Usuario usuarioNovo = DAO.save(usuario);
        return usuarioNovo;
    }

    public Usuario editarUsuario(Usuario usuario){
        if (!DAO.existsById(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        String encoder = this.passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(encoder);
        return DAO.save(usuario);
    }

    public Boolean excluirUsuario(Integer id){
        DAO.deleteById(id);
        return true;
    }

    public Boolean validarSenha(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getSenha() == null){
            return false;

        }
        return DAO.findByEmail(usuario.getEmail())
                .map(u -> passwordEncoder.matches(usuario.getSenha(),u.getSenha()))
                .orElse(false);
    }

    public Usuario login(Usuario usuario){
        if(usuario.getEmail() == null || usuario.getSenha() == null){
            return null;
        }
        return DAO.findByEmail(usuario.getEmail())
                .filter(u -> passwordEncoder.matches(usuario.getSenha(),u.getSenha()))
                .orElse(null);
    }

    public Usuario buscarPorId(Integer id) {
        return DAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

}
