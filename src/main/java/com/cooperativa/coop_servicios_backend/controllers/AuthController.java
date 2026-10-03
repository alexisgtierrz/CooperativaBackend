package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.DTOs.LoginRequest;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.security.JwtUtil;
import com.cooperativa.coop_servicios_backend.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Buscamos si existe un usuario con ese email
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorEmail(request.getEmail());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();

        // Comparamos la contraseña enviada con el Hash guardado en la base de datos
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        List<GrantedAuthority> autoridades = usuario.getPerfil().getPermisos().stream()
                .map(permiso -> new SimpleGrantedAuthority(permiso.getNombre()))
                .collect(Collectors.toList());

        // Generamos el Token JWT inyectando las autoridades
        String token = jwtUtil.generarToken(usuario.getEmail(), autoridades);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", usuario.getEmail());

        response.put("perfil", usuario.getPerfil().getNombre());

        return ResponseEntity.ok(response);
    }
}