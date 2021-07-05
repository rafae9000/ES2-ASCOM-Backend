package com.ES2.ASCOM.repository;

import java.time.LocalDate;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.enums.Feedback;
import com.ES2.ASCOM.enums.Noticia_portal_local;
import com.ES2.ASCOM.enums.Status_clipping;
import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.pagination.Paginacao;

@Repository
public class CustomClippingDAO {
	private final EntityManager em;
	private final Paginacao<Clipping> pag;

    public CustomClippingDAO(EntityManager em) {
        this.em = em;
        this.pag = new Paginacao<Clipping>();
    }
    
    public Map<String,Object> listagemCustomizada(Integer usuarioCriadorId, Integer usuarioAtribuidoId,
												  Integer generoJornalisticoId, String titulo,
												  String editora, String classificacao,
												  String subclassificacao, Feedback feedback,
												  Noticia_portal_local noticiaPortalLocal,
												  Status_clipping status, String isVinculado,
												  LocalDate data_inicial, LocalDate data_final,
												  String ordenacao_id, Integer paginaAtual, 
												  Integer tamanhoPagina) throws ApiRequestException{
    	
    	if(ordenacao_id == null) ordenacao_id = "desc";
    	String query = "select clipping from Clipping as clipping ";
    	
    	QueryBuilder<Clipping> queryBuilder = new QueryBuilder<Clipping>();
    	queryBuilder.setSelect(query);
    	queryBuilder.addConditionLike("clipping.titulo_materia", "titulo", titulo);
    	queryBuilder.addConditionLike("clipping.editora", "editora", editora);
    	queryBuilder.addConditionLike("clipping.classificacao", "classificacao", classificacao);
    	queryBuilder.addConditionLike("clipping.subclassificacao", "subclassificacao", subclassificacao);
    	queryBuilder.addConditionEquals("clipping.feedback","feedback", feedback);
    	queryBuilder.addConditionEquals("clipping.noticia_portal_local","noticiaPortalLocal", noticiaPortalLocal);
    	queryBuilder.addConditionEquals("clipping.status","status", status);
    	queryBuilder.addConditionEquals("clipping.usuario.id","usuarioCriadorId", usuarioCriadorId);
    	
    	if(usuarioAtribuidoId != null && usuarioAtribuidoId == 0) {
    		queryBuilder.addConditionIsNull("clipping.usuario_atribuido.id");
    	}
    	else {
    		queryBuilder.addConditionEquals("clipping.usuario_atribuido.id","usuarioAtribuidoId", usuarioAtribuidoId);
    	}
    	
    	if(isVinculado != null) {
    		if(isVinculado.equals("true")) {
    			queryBuilder.addConditionIsNotNull("clipping.chamado.id");
    		}
    		else {
    			queryBuilder.addConditionIsNull("clipping.chamado.id");
    		}
    	}
    	
    	if(data_inicial != null || data_final != null) {
    		if (data_inicial == null) data_inicial = LocalDate.parse("2021-06-01");
    		if (data_final == null) data_final = LocalDate.now();
    		queryBuilder.addConditionBetween("chamado.data_publicacao", "data_inicial", data_inicial, "data_final", data_final);
    	}
    	queryBuilder.addConditionOrderBy("clipping.id", ordenacao_id);
    	
    	
    	TypedQuery<Clipping> tQuery = queryBuilder.buildQuery(em, Clipping.class);
    	
    	return pag.paginarLista(paginaAtual, tamanhoPagina, tQuery.getResultList());
    }
}


