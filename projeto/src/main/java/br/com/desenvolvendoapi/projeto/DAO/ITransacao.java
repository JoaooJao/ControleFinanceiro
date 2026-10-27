package br.com.desenvolvendoapi.projeto.DAO;

import br.com.desenvolvendoapi.projeto.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITransacao extends JpaRepository<Transacao, Integer> {
    List<Transacao> findByUsuarioIdOrderByDataDesc(Integer usuarioID);

    List<Transacao> findByUsuarioIdAndDataBetweenOrderByDataDesc(
            Integer usuarioId, LocalDate inicio, LocalDate fim);

    Optional<Transacao> findByIdAndUsuarioId(Integer id, Integer usuarioId);
}
