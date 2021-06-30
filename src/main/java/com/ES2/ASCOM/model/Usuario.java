package com.ES2.ASCOM.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario", schema = "ascom")
public class Usuario {

	@Id
	@Column(name = "usuario_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Email
	@Size(max = 50, message = "Tamanho do email deve ser no maximo 50 caracteres")
	@Column(name = "email")
	private String email;

	@NotBlank(message = "Nome inválido")
	@Size(max = 50, message = "Tamanho do nome deve ser no maximo 50 caracteres")
	@Column(name = "nome")
	private String nome;

	@JsonIgnore
	@Column(name = "senha")
	private String senha;

	@NotBlank(message = "Profissão inválida")
	@Size(max = 30, message = "Tamanho da profissão deve ser no maximo 30 caracteres")
	@Column(name = "profissao")
	private String profissao;

	@Column(name = "ativo")
	private boolean ativo;
	
	@JsonIgnore
	@Column(name = "token_reseta_senha", nullable = true)
	private String tokenResetaSenha;

	@ManyToOne
	@JoinColumn(name = "grupo_id", nullable = false)
	private Grupo grupo;
	
	@JsonIgnore
	@OneToMany(mappedBy="usuario") 
	private List<Chamado> chamados;
	
	@JsonIgnore
	@OneToMany(mappedBy="usuario") 
	private List<Clipping> clippings;
	 

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getProfissao() {
		return profissao;
	}

	public void setProfissao(String profissao) {
		this.profissao = profissao;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Grupo getGrupo() {
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public String getTokenResetaSenha() {
		return tokenResetaSenha;
	}

	public void setTokenResetaSenha(String tokenResetaSenha) {
		this.tokenResetaSenha = tokenResetaSenha;
	}

	public List<Chamado> getChamados() {
		return chamados;
	}

	public void setChamados(List<Chamado> chamados) {
		this.chamados = chamados;
	}

	public List<Clipping> getClippings() {
		return clippings;
	}

	public void setClippings(List<Clipping> clippings) {
		this.clippings = clippings;
	}

}
