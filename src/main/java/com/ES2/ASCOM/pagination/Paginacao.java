package com.ES2.ASCOM.pagination;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.ES2.ASCOM.exception.ApiRequestException;

public class Paginacao<T> {
	
	private static final Integer PAGINA_INICIAL = 1;
	private static final Integer TAMANHO_DA_PAGINA = 10;
	
	public Map<String,Object>  paginarLista(Integer paginaAtual, Integer tamanhoPagina, List<T> lista) {
		Map<String,Object> result = new HashMap<String,Object>();
		int tamanhoLista = lista.size();
		if(tamanhoLista == 0) {
			result.put("paginaAtual",1);
			result.put("paginasTotais",1);
			result.put("conteudo",lista);
			return result;
		}
		if(paginaAtual == null)
			paginaAtual = PAGINA_INICIAL;
		
		if(tamanhoPagina == null)
			tamanhoPagina = TAMANHO_DA_PAGINA;
		
		if(paginaAtual < 0)
			throw new ApiRequestException("Pagina atual deve ser um numero inteiro positivo",
					HttpStatus.BAD_REQUEST);
		
		if(tamanhoPagina < 0)
			throw new ApiRequestException("Tamanho da Pagina deve ser um numero inteiro positivo",
					HttpStatus.BAD_REQUEST);
		
		double aux2 = Math.ceil((double)tamanhoLista/(double)tamanhoPagina);
		int paginasTotais  = (int) aux2;
		
		if(paginasTotais < paginaAtual)
			throw new ApiRequestException("Foi pedido a pagina de n° "+paginaAtual+
					" ,no entanto so existem "+paginasTotais+" paginas",
					HttpStatus.BAD_REQUEST);
		
		int inicio = (paginaAtual - 1) * tamanhoPagina;
		int fim = inicio + tamanhoPagina;
		
		if(fim > tamanhoLista)
			fim = tamanhoLista;
		
		List<T> listaPag = lista.subList(inicio, fim);
		
		result.put("paginaAtual",paginaAtual);
		result.put("paginasTotais",paginasTotais);
		result.put("conteudo",listaPag);
		
		return result;
	}

}
