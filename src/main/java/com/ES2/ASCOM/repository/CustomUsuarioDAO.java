package com.ES2.ASCOM.repository;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.pagination.Paginacao;

@Repository
public class CustomUsuarioDAO {
	
	private final EntityManager em;
	private final Paginacao<Usuario> pag;

    public CustomUsuarioDAO(EntityManager em) {
        this.em = em;
        this.pag = new Paginacao<Usuario>();
    }
    
    public Map<String,Object> listagemCustomizada(String nome, String email, String profissao,String ativo,
    											  Integer grupo_id, String ordenacao_nome, Integer paginaAtual,
    											  Integer tamanhoPagina ){
    	
    	String query = "select user from Usuario as user ";
    	
    	QueryBuilder<Usuario> queryBuilder = new QueryBuilder<Usuario>();
    	queryBuilder.setSelect(query);
    	queryBuilder.addConditionLike("user.nome", "nome", nome);
    	queryBuilder.addConditionLike("user.email", "email", email);
    	queryBuilder.addConditionLike("user.profissao", "profissao", profissao);
    	queryBuilder.addConditionEquals("user.grupo.id","grupo_id", grupo_id);
    	queryBuilder.addConditionOrderBy("user.nome", ordenacao_nome);
    	if(ativo != null) {
    		boolean active = Boolean.parseBoolean(ativo);
    		queryBuilder.addConditionEquals("user.ativo","ativo", active);
    	}
    	
    	TypedQuery<Usuario> tQuery = queryBuilder.buildQuery(em, Usuario.class);
    	tQuery.getResultList().forEach(usuario -> usuario.setSenha(null));
    	
    	return pag.paginarLista(paginaAtual, tamanhoPagina, tQuery.getResultList());
    }
    
    
    
}
