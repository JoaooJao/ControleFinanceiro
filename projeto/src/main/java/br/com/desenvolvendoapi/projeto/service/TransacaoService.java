package br.com.desenvolvendoapi.projeto.service;

import br.com.desenvolvendoapi.projeto.DAO.ITransacao;
import br.com.desenvolvendoapi.projeto.DAO.IUsuario;
import br.com.desenvolvendoapi.projeto.model.ResumoFinanceiro;
import br.com.desenvolvendoapi.projeto.model.TipoTransacao;
import br.com.desenvolvendoapi.projeto.model.Transacao;
import br.com.desenvolvendoapi.projeto.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    private final ITransacao transacaoDAO;
    private final IUsuario usuarioDAO;

    public TransacaoService(ITransacao transacaoDAO, IUsuario usuarioDAO) {
        this.transacaoDAO = transacaoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public List<Transacao> listar(Integer usuarioId, Integer mes, Integer ano) {
        if (mes != null && ano != null) {
            YearMonth periodo = YearMonth.of(ano, mes);
            return transacaoDAO.findByUsuarioIdAndDataBetweenOrderByDataDesc(
                    usuarioId, periodo.atDay(1), periodo.atEndOfMonth());
        }
        return transacaoDAO.findByUsuarioIdOrderByDataDesc(usuarioId);
    }

    public Transacao criar(Integer usuarioId, Transacao transacao) {
        Usuario usuario = usuarioDAO.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        transacao.setId(null);
        transacao.setUsuario(usuario);
        return transacaoDAO.save(transacao);
    }

    public Transacao editar(Integer usuarioId, Integer id, Transacao dados) {
        Transacao existente = transacaoDAO.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transação não encontrada"));
        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setTipo(dados.getTipo());
        existente.setCategoria(dados.getCategoria());
        existente.setData(dados.getData());
        return transacaoDAO.save(existente);
    }

    public void excluir(Integer usuarioId, Integer id) {
        Transacao existente = transacaoDAO.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transação não encontrada"));
        transacaoDAO.delete(existente);
    }

    public ResumoFinanceiro resumo(Integer usuarioId, Integer mes, Integer ano) {
        List<Transacao> lista = listar(usuarioId, mes, ano);

        BigDecimal receitas = somar(lista, TipoTransacao.RECEITA);
        BigDecimal despesas = somar(lista, TipoTransacao.DESPESA);

        Map<String, BigDecimal> porCategoria = lista.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
                .collect(Collectors.groupingBy(
                        Transacao::getCategoria,
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)));

        return new ResumoFinanceiro(receitas, despesas, receitas.subtract(despesas), porCategoria);
    }

    private BigDecimal somar(List<Transacao> lista, TipoTransacao tipo) {
        return lista.stream()
                .filter(t -> t.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}