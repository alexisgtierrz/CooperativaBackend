package com.cooperativa.coop_servicios_backend.config;

import com.cooperativa.coop_servicios_backend.models.Perfil;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.PerfilRepository;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepo, PerfilRepository perfilRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            //Solo insertamos si la base de datos de usuarios está vacía
            if (usuarioRepo.count() == 0) {

                //Creamos el perfil administrador
                Perfil perfilAdmin = new Perfil();
                perfilAdmin.setNombre("Administrador");
                //Al guardarlo, se le genera el ID que necesita el Usuario
                perfilAdmin = perfilRepo.save(perfilAdmin);

                //Creamos el usuario inicial
                Usuario admin = new Usuario();
                admin.setEmail("admin@coop.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setActivo(true);
                admin.setPerfil(perfilAdmin);

                usuarioRepo.save(admin);

                System.out.println("==================================================");
                System.out.println("✅ USUARIO ADMIN CREADO PARA POSTMAN");
                System.out.println("Email: admin@coop.com");
                System.out.println("Password: 123456");
                System.out.println("==================================================");
            }
        };
    }
}