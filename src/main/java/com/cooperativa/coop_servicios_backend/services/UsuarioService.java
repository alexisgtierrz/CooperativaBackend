package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    public List<Usuario> obtenerTodos() { return repository.findAll(); }
    public Optional<Usuario> obtenerPorId(Long id) { return repository.findById(id); }

    // Metodo para buscar al usuario cuando intente loguearse
    public Optional<Usuario> obtenerPorEmail(String email) { return repository.findByEmail(email); }

    public Usuario guardar(Usuario usuario) { return repository.save(usuario); }
    public void eliminar(Long id) { repository.deleteById(id); }
}
