package com.cooperativa.coop_servicios_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Obtenemos la cabecera "Authorization" de la petición
        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extraemos solo el token, quitando la palabra "Bearer "
            try {
                email = jwtUtil.extraerEmail(jwt);
            } catch (Exception e) {
                System.out.println("Error al extraer el email del token: " + e.getMessage());
            }
        }

        // Si encontramos un email y aún no hay una autenticación activa en este contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validamos que el token no esté vencido y corresponda al email
            if (jwtUtil.validarToken(jwt, email)) {

                // Extraemos los perfiles del token
                List<String> permisosNombres = jwtUtil.extraerPermisos(jwt);

                // Los convertimos
                List<GrantedAuthority> authorities = permisosNombres != null ?
                        permisosNombres.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                        : List.of();

                // Se crea el token de autenticacion
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}