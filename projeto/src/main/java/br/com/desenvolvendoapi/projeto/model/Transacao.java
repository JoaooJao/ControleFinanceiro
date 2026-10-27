package br.com.desenvolvendoapi.projeto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter

@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Insira uma descrição")
    @Column(name = "descricao", length = 200)
    private String descricao;

    @NotNull(message = "Insira um valor")
    @Positive(message = "O valor deve ser maior que zero")
    @Column(name = "valor", precision = 12, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Informe o tipo (RECEITA ou DESPESA)")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 10)
    private TipoTransacao tipo;

    @NotBlank(message = "Insira uma categoria")
    @Column(name = "categoria", length = 50)
    private String categoria;

    @NotNull(message = "Insira a data")
    @Column(name = "data")
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;
}