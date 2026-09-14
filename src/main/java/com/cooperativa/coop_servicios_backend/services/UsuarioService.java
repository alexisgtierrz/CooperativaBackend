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
        //Verificamos que traiga contraseña y que no esté ya encriptada, para evitar que volvamos a encriptar algo que ya estaba encriptado si hacemos un PUT.
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            String hash = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(hash);
        }
        return repository.save(usuario);
    }
    public void eliminar(Long id) { repository.deleteById(id); }
}
