package br.com.desenvolvendoapi.projeto.controller;

import br.com.desenvolvendoapi.projeto.model.ResumoFinanceiro;
import br.com.desenvolvendoapi.projeto.model.Transacao;
import br.com.desenvolvendoapi.projeto.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    private Integer usuarioId(Jwt jwt) {
        return Integer.valueOf(jwt.getSubject());
    }

    @GetMapping
    public ResponseEntity<List<Transacao>> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(transacaoService.listar(usuarioId(jwt), mes, ano));
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiro> resumo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(transacaoService.resumo(usuarioId(jwt), mes, ano));
    }

    @PostMapping
    public ResponseEntity<Transacao> criar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Transacao transacao) {
        return ResponseEntity.status(201).body(transacaoService.criar(usuarioId(jwt), transacao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transacao> editar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @Valid @RequestBody Transacao transacao) {
        return ResponseEntity.ok(transacaoService.editar(usuarioId(jwt), id, transacao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {
        transacaoService.excluir(usuarioId(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
