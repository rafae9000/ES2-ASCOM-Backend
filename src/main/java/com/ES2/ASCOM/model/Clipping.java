
package com.ES2.ASCOM.model;

import java.util.Date;
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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.ES2.ASCOM.enums.Feedback;
import com.ES2.ASCOM.enums.Noticia_portal_local;
import com.ES2.ASCOM.enums.Status_clipping;
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
	
	@OneToOne()
    @JoinColumn(name = "chamado_id", referencedColumnName = "chamado_id", nullable=true)
    private Chamado chamado;
	
	@ManyToOne
	@JoinColumn(name = "genero_jornalistico_id", nullable=false)
	private Genero_jornalistico genero_jornalistico;
	
	@Column(name = "data_publicacao")
	private Date data_publicacao;
	
	//sim ou nao
	@Enumerated(EnumType.STRING)
	@Column(name = "noticia_portal_local")
	private Noticia_portal_local noticia_portal_local;
	
	@Column(name = "titulo_materia")
	private String titulo_materia;
	
	@Column(name = "manteve_titulo")
	private String manteve_titulo;
	
	@Column(name = "subtitulo_materia")
	private String subtitulo_materia;
	
	@Column(name = "manteve_subtitulo")
	private String manteve_subtitulo;
	
	@Column(name = "editora")
	private String editora;
	
	@Lob
	@Column(name = "texto_integral")
	private String texto_integral;
	
	@Column(name = "manteve_texto")
	private String manteve_texto;
	
	@Column(name = "manifestacao_credito")
	private String manifestacao_credito;
	
	@Column(name = "classificacao")
	private String classificacao;
	
	@Column(name = "subclassificacao")
	private String subclassificacao;
	
	@Column(name = "pessoas_citadas")
	private String pessoas_citadas;
	
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
	private List<Arquivo_clipping> arquivos;
}

