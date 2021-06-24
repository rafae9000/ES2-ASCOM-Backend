package com.ES2.ASCOM.helpers;

import java.util.Date;


import org.springframework.http.HttpStatus;

import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.model.Usuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;


public class TokenService {
	
	private static final long expirationTime = 4 * 60 * 60 * 1000;
	
	private String key = "jf8eqmdiw0dwfrhtreh68";
	
	public String generateToken(Usuario user) {
		return Jwts.builder()
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setSubject(user.getId().toString())
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(SignatureAlgorithm.HS256, key)
				.compact();
	}
	
	public Claims decodeToken(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(key)
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public Claims validateToken(String token) throws ApiRequestException {
		Claims claims = decodeToken(token);
		
		if(claims == null)
			throw new ApiRequestException("Token invalido",HttpStatus.UNAUTHORIZED);
		
		if(claims.getExpiration().before(new Date(System.currentTimeMillis())))
			throw new ApiRequestException("Token expirado",HttpStatus.UNAUTHORIZED);
		
		return claims;
	}
	
	public Integer getTokenSubject(String token) throws ApiRequestException {
		Claims claims = validateToken(token);
		Integer id = Integer.valueOf(claims.getSubject());
		return id;
	}
}
