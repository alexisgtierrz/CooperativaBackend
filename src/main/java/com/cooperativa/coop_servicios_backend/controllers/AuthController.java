package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.DTOs.LoginRequest;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.security.JwtUtil;
import com.cooperativa.coop_servicios_backend.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        //Buscamos si existe un usuario con ese email
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorEmail(request.getEmail());

        //Si no existe, devolvemos error 401
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();

        //Comparamos la contraseña enviada con el Hash guardado en la base de datos
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        //Si esta bien, generamos el Token JWT
        String token = jwtUtil.generarToken(usuario.getEmail());

        //Armamos una respuesta en formato JSON para que el frontend lo pueda leer fácilmente
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", usuario.getEmail());

        return ResponseEntity.ok(response);
    }
}
