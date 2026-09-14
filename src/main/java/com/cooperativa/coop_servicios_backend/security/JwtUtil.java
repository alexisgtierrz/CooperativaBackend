package com.cooperativa.coop_servicios_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    //Clave secreta para firmar el token. Debe ser larga y segura para que el algoritmo HS256 funcione.
    private static final String SECRET_KEY_STRING = "EstaEsUnaClaveSecretaMuyLargaYSeguraParaNuestraCooperativa2026";
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    //Tiempo de validez del token: 10 horas (en milisegundos)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    //Metodo para generar el token usando el email del usuario
    public String generarToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    //Metodo para extraer el email (subject) de un token que nos manda el frontend
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    //Metodo para validar si el token es correcto y no está vencido
    public Boolean validarToken(String token, String emailUsuario) {
        final String email = extraerEmail(token);
        return (email.equals(emailUsuario) && !tokenExpirado(token));
    }

    private <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodasLasClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodasLasClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean tokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }
}