
package com.ES2.ASCOM.model;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ES2.ASCOM.enums.Status_chamado;
import com.ES2.ASCOM.enums.Tipo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chamado", schema = "ascom")
public class Chamado {
	
	/**
	 * All of the annotations used in the example are standard JSR annotations:

	@NotNull validates that the annotated property value is not null.
	@AssertTrue validates that the annotated property value is true.
	@Size validates that the annotated property value has a size between the attributes min and max; can be applied to String, Collection, Map, and array properties.
	@Min validates that the annotated property has a value no smaller than the value attribute.
	@Max validates that the annotated property has a value no larger than the value attribute.
	@Email validates that the annotated property is a valid email address.
	Some annotations accept additional attributes, but the message attribute is common to all of them. This is the message that will usually be rendered when the value of the respective property fails validation.
	
	And some additional annotations that can be found in the JSR:
	
	@NotEmpty validates that the property is not null or empty; can be applied to String, Collection, Map or Array values.
	@NotBlank can be applied only to text values and validates that the property is not null or whitespace.
	@Positive and @PositiveOrZero apply to numeric values and validate that they are strictly positive, or positive including 0.
	@Negative and @NegativeOrZero apply to numeric values and validate that they are strictly negative, or negative including 0.
	@Past and @PastOrPresent validate that a date value is in the past or the past including the present; can be applied to date types including those added in Java 8.
	@Future and @FutureOrPresent validate that a date value is in the future, or in the future including the present.

	 * */
	@Id
	@Column(name = "chamado_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = true)
	private Usuario usuario;
	
	@ManyToOne
	@JoinColumn(name = "usuario_atribuido_id", nullable = true)
	private Usuario usuario_atribuido;
	
	@JsonIgnore
	@OneToOne(mappedBy = "chamado")
    private Clipping clipping;
	
	@Column(name = "titulo")
	@NotBlank(message = "Titulo invalido")//100
	@Size(max = 100, message = "Tamanho do titulo deve ser no maximo 100 caracteres")
	private String titulo;
	
	@NotBlank(message = "Nome invalido")
	@Size(max = 50, message = "Tamanho do nome deve ser no maximo 50 caracteres")
	@Column(name = "nome")//50
	private String nome;

	@NotBlank(message = "Setor invalido")
	@Size(max = 50, message = "Tamanho do nome deve ser no maximo 50 caracteres")
	@Column(name = "setor")//50
	private String setor;
	
	@NotBlank(message = "Telefone invalido")
	@Size(min = 9, max = 25, message = "Tamanho do telefone deve está entre 9 e 25 caracteres")
	@Column(name = "telefone")//25
	private String telefone;
	
	@Email(message = "Email invalido")
	@Size(max = 50, message = "Tamanho do email deve ser no maximo 50 caracteres")
	@Column(name = "email")//50
	private String email;
	
	@NotBlank(message = "Ramal invalido")
	@Size(max = 50, message = "Tamanho do ramal deve ser no maximo 50 caracteres")
	@Column(name = "ramal")//50
	private String ramal;
	
	@NotBlank(message = "Detalhes de solicitacao invalido")
	@Size(max = 250, message = "Tamanho dos detalhes de solicitacao deve ser no maximo 250 caracteres")
	@Column(name = "detalhes_solicitacao")//250
	private String detalhes_solicitacao;
	
	@NotBlank(message = "Centro custo requisição invalido")
	@Size(max = 100, message = "Tamanho do centro custo requisição deve ser no maximo 100 caracteres")
	@Column(name = "centro_custo_requisicao")//100
	private String centro_custo_requisicao;
	
	@NotBlank(message = "Localização invalida")
	@Size(max = 100, message = "Tamanho da localização deve ser no maximo 100 caracteres")
	@Column(name = "localizacao")//100
	private String localizacao;
	
	//aberto,fechado ou anulado
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status_chamado status;
	
	//requisicao ou incidente
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo")
	private Tipo tipo;
	
	@Column(name = "data_publicacao")
	private LocalDate data_publicacao;
	
	@Size(max = 250, message = "Tamanho da justificativa deve ser no maximo 250 caracteres")
	@Column(name = "justificativa", nullable = true)//250
	private String justificativa;

}

