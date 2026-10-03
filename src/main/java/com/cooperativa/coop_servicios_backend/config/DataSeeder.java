package com.cooperativa.coop_servicios_backend.config;

import com.cooperativa.coop_servicios_backend.models.Perfil;
import com.cooperativa.coop_servicios_backend.models.Permiso;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.PerfilRepository;
import com.cooperativa.coop_servicios_backend.repositories.PermisoRepository;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private PermisoRepository permisoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepo, PerfilRepository perfilRepo, PermisoRepository permisoRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            // Solo insertamos si la base de datos de usuarios está vacía
            if (usuarioRepo.count() == 0) {

                // Nombres de los permisos que vamos a crear
                List<String> nombresPermisos = Arrays.asList(
                        "GESTIONAR_CLIENTES",
                        "MODIFICAR_CLIENTES",
                        "CREAR_CLIENTES",
                        "ELIMINAR_CLIENTES",
                        "VER_CLIENTES",
                        "GESTIONAR_USUARIOS"
                );

                List<Permiso> permisosGuardados = new ArrayList<>();

                // Guardar cada permiso en la tabla 'permisos'
                for (String nombre : nombresPermisos) {
                    Permiso permiso = new Permiso();
                    permiso.setNombre(nombre);
                    permiso = permisoRepo.save(permiso);
                    permisosGuardados.add(permiso);
                }
                // Creamos el perfil administrador y le inyectamos la lista de permisos
                Perfil perfilAdmin = new Perfil();
                perfilAdmin.setNombre("Administrador");
                perfilAdmin.setPermisos(permisosGuardados);

                perfilAdmin = perfilRepo.save(perfilAdmin);

                // Creamos el usuario inicial y le asignamos el perfil ya completo
                Usuario admin = new Usuario();
                admin.setEmail("admin@coop.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setActivo(true);
                admin.setPerfil(perfilAdmin);

                usuarioRepo.save(admin);

                System.out.println("==================================================");
                System.out.println("✅ PERMISOS, PERFIL Y USUARIO ADMIN CREADOS");
                System.out.println("Email: admin@coop.com");
                System.out.println("Password: 123456");
                System.out.println("==================================================");
            }
        };
    }
}