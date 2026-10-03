package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() { return repository.findAll(); }
    public Optional<Usuario> obtenerPorId(Long id) { return repository.findById(id); }

    //Metodo para buscar al usuario cuando intente loguearse
    public Optional<Usuario> obtenerPorEmail(String email) { return repository.findByEmail(email); }

    public Usuario guardar(Usuario usuario) {
        // 1. Encriptar la contraseña antes de guardarla si no viene encriptada
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        // 2. Asegurar que esté activo por defecto
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        return repository.save(usuario);
    }
    public void eliminar(Long id) { repository.deleteById(id); }
}
