
package com.ES2.ASCOM.model;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.ES2.ASCOM.enums.Feedback;
import com.ES2.ASCOM.enums.Noticia_portal_local;
import com.ES2.ASCOM.enums.Status_clipping;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clipping", schema = "ascom")
public class Clipping {
	
	@Id
	@Column(name = "clipping_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;
	
	@ManyToOne
	@JoinColumn(name = "usuario_atribuido_id", nullable = true)
	private Usuario usuario_atribuido;
	
	@JsonIgnore
	@OneToOne()
    @JoinColumn(name = "chamado_id", referencedColumnName = "chamado_id", nullable=true)
    private Chamado chamado;
	
	@ManyToOne
	@JoinColumn(name = "genero_jornalistico_id", nullable=false)
	private GeneroJornalistico genero_jornalistico;
	
	@Column(name = "data_publicacao")
	private LocalDate data_publicacao;
	
	//sim ou nao
	@Enumerated(EnumType.STRING)
	@Column(name = "noticia_portal_local")
	private Noticia_portal_local noticia_portal_local;
	
	@NotBlank(message = "Titulo da materia inválido")
	@Size(max = 100, message = "Tamanho do titulo deve ser no maximo 100 caracteres")
	@Column(name = "titulo_materia")
	private String titulo_materia;
	
	@NotBlank(message = "Campo manteve titulo inválido")
	@Size(max = 100, message = "Tamanho do campo manteve titulo deve ser no maximo 100 caracteres")
	@Column(name = "manteve_titulo")
	private String manteve_titulo;
	
	@NotBlank(message = "Subtitulo da materia inválido")
	@Size(max = 100, message = "Tamanho do subtitulo deve ser no maximo 100 caracteres")
	@Column(name = "subtitulo_materia")
	private String subtitulo_materia;
	
	@NotBlank(message = "Campo manteve subtitulo inválido")
	@Size(max = 100, message = "Tamanho do campo manteve subtitulo deve ser no maximo 100 caracteres")
	@Column(name = "manteve_subtitulo")
	private String manteve_subtitulo;
	
	@NotBlank(message = "Campo manteve subtitulo inválido")
	@Size(max = 50, message = "Tamanho do campo manteve subtitulo deve ser no maximo 50 caracteres")
	@Column(name = "editora")
	private String editora;
	
	@Column(name = "texto_integral",columnDefinition="TEXT")
	private String texto_integral;
	
	@Column(name = "manteve_texto",columnDefinition="TEXT")
	private String manteve_texto;
	
	@NotBlank(message = "Manifestação de credito inválida")
	@Size(max = 250, message = "Tamanho da manifestação de credito deve ser no maximo 250 caracteres")
	@Column(name = "manifestacao_credito")
	private String manifestacao_credito;
	
	@NotBlank(message = "Classificação inválida")
	@Size(max = 50, message = "Tamanho da classificação deve ser no maximo 50 caracteres")
	@Column(name = "classificacao")
	private String classificacao;
	
	@NotBlank(message = "Subclassificação de credito inválida")
	@Size(max = 50, message = "Tamanho da subclassificação deve ser no maximo 50 caracteres")
	@Column(name = "subclassificacao")
	private String subclassificacao;
	
	@NotBlank(message = "Campo pessoas citadas inválido")
	@Size(max = 150, message = "Tamanho do campo pessoas citadas deve ser no maximo 150 caracteres")
	@Column(name = "pessoas_citadas")
	private String pessoas_citadas;
	
	@NotBlank(message = "Campo url inválido")
	@Size(max = 300, message = "Tamanho do campo url deve ser no maximo 300 caracteres")
	@Column(name = "url")
	private String url;
	
	@Column(name = "observacoes")
	private String observacoes;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status_clipping status;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "feedback")
	private Feedback feedback;
	
	@Column(name = "justificativa", nullable = true)
	private String justificativa;
	
	@JsonIgnore
	@OneToMany(mappedBy = "clipping")
	private List<ArquivoClipping> arquivos;
}

