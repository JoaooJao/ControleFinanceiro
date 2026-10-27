package br.com.desenvolvendoapi.projeto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Insira um Nome")
    @Column(name = "nome", length = 200)
    private String nome;

    @Email(message = "Insira um email válido")
    @NotBlank(message = "Insira um Email")
    @Column(name = "email", length = 200,unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Insira uma senha")
    @Column(name = "senha", columnDefinition = "TEXT")
    private String senha;

    @NotBlank(message = "Insira um telefone")
    @Column(name = "telefone", length = 15)
    private String telefone;

}
