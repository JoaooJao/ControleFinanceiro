package br.com.desenvolvendoapi.projeto.controller;

import br.com.desenvolvendoapi.projeto.model.LoginResponse;
import br.com.desenvolvendoapi.projeto.model.Usuario;
import br.com.desenvolvendoapi.projeto.service.TokenService;
import br.com.desenvolvendoapi.projeto.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private UsuarioService usuarioService;
    private TokenService tokenService;

    public UsuarioController(UsuarioService usuarioService, TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    // Antes listava todos os usuários. Agora devolve só os dados de quem está logado.
    @GetMapping("/me")
    public ResponseEntity<Usuario> meusDados(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.buscarPorId(Integer.valueOf(jwt.getSubject())));
    }

    @PostMapping
    public ResponseEntity<Usuario> criarUsuario(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(201).body(usuarioService.criarUsuario(usuario));
    }

    // O id vem do token, o id enviado no JSON é ignorado
    @PutMapping
    public ResponseEntity<Usuario> editarUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Usuario usuario) {
        usuario.setId(Integer.valueOf(jwt.getSubject()));
        return ResponseEntity.status(200).body(usuarioService.editarUsuario(usuario));
    }

    // Só permite excluir a própria conta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {
        if (!id.equals(Integer.valueOf(jwt.getSubject()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        usuarioService.excluirUsuario(id);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> validarSenha(@RequestBody Usuario usuario) {
        Usuario logado = usuarioService.login(usuario);
        if (logado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new LoginResponse(tokenService.gerarToken(logado), logado));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;

    }
}