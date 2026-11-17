package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Empleado;
import com.cooperativa.coop_servicios_backend.models.Perfil;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.EmpleadoRepository;
import com.cooperativa.coop_servicios_backend.repositories.PerfilRepository;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EspecialidadService especialidadService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Empleado> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Empleado> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Empleado guardar(Empleado empleado) {
        boolean esNuevo = empleado.getId() == null;

        //La especialidad debe ser un puesto
        empleado.setEspecialidad(especialidadService.validarYNormalizar(empleado.getEspecialidad()));

        if (esNuevo) {
            if (empleado.getDni() != null && repository.existsByDni(empleado.getDni())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un empleado con el DNI " + empleado.getDni());
            }

            //Legajo secuencial de 3 digitos
            Integer maxLegajo = repository.findMaxLegajoNumerico();
            int siguiente = (maxLegajo == null ? 0 : maxLegajo) + 1;
            empleado.setLegajo(String.format("%03d", siguiente));

            //Dejar la secuencia de IDs por encima del MAX(id) real
            sincronizarSecuenciaId();
        }

        //Usuario asociado
        if (empleado.getUsuario() == null) {
            empleado.setUsuario(obtenerOCrearUsuario(empleado));
        }

        return repository.save(empleado);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    private Usuario obtenerOCrearUsuario(Empleado empleado) {
        String nombreLimpio = limpiarTexto(empleado.getNombre());
        String apellidoLimpio = limpiarTexto(empleado.getApellido());

        String baseEmail = nombreLimpio + "." + apellidoLimpio;
        String email = baseEmail + "@empleado.coop";

        //Si el email ya existe en la BD:
        //Si está libre (no asociado a otro empleado), lo reutilizamos.
        //Si ya está ocupado por otro empleado, probamos con un sufijo numérico
        int contador = 2;
        while (true) {
            Optional<Usuario> existente = usuarioRepository.findByEmail(email);
            if (existente.isEmpty()) {
                break; //Email disponible para crear usuario nuevo
            }

            Usuario usuarioExistente = existente.get();
            if (!repository.existsByUsuarioId(usuarioExistente.getId())) {
                return usuarioExistente; //Usuario huérfano existente, lo reutilizamos
            }

            email = baseEmail + contador + "@empleado.coop";
            contador++;
        }

        //Se crea el usuario nuevo con el email disponible
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(empleado.getDni()));
        nuevoUsuario.setActivo(true);

        Perfil perfilEmpleado = obtenerOCrearPerfilEmpleado();

        nuevoUsuario.setPerfil(perfilEmpleado);

        return usuarioRepository.save(nuevoUsuario);
    }

    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        String sinAcentos = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private Perfil obtenerOCrearPerfilEmpleado() {
        return perfilRepository.findByNombre("EMPLEADO")
                .or(() -> perfilRepository.findByNombre("ROLE_EMPLEADO"))
                .or(() -> perfilRepository.findByNombre("Empleado"))
                .orElseGet(() -> {
                    Perfil perfil = new Perfil();
                    perfil.setNombre("EMPLEADO");
                    return perfilRepository.save(perfil);
                });
    }

    private void sincronizarSecuenciaId() {
        entityManager.createNativeQuery(
                "SELECT setval(pg_get_serial_sequence('empleados', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM empleados), 0) + 1, false)")
                .getSingleResult();
    }
}
