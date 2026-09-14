package com.cooperativa.coop_servicios_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //Obtenemos la cabecera "Authorization" de la petición
        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        //Verificamos que la cabecera exista y empiece con "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extraemos solo el token, quitando la palabra "Bearer "
            try {
                email = jwtUtil.extraerEmail(jwt);
            } catch (Exception e) {
                System.out.println("Error al extraer el email del token: " + e.getMessage());
            }
        }

        //Si encontramos un email y aún no hay una autenticación activa en este contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            //Validamos que el token no esté vencido y corresponda al email
            if (jwtUtil.validarToken(jwt, email)) {

                //Creamos el "pase libre" oficial de Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, new ArrayList<>());

                //Registramos al usuario como autenticado
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        //Continuar la petición hacia el Controlador
        filterChain.doFilter(request, response);
    }
}