package com.cooperativa.coop_servicios_backend.config;

import com.cooperativa.coop_servicios_backend.models.Empleado;
import com.cooperativa.coop_servicios_backend.models.Especialidad;
import com.cooperativa.coop_servicios_backend.models.Estado;
import com.cooperativa.coop_servicios_backend.models.Perfil;
import com.cooperativa.coop_servicios_backend.models.Permiso;
import com.cooperativa.coop_servicios_backend.models.TipoIncidenteTecnico;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.EmpleadoRepository;
import com.cooperativa.coop_servicios_backend.repositories.EspecialidadRepository;
import com.cooperativa.coop_servicios_backend.repositories.EstadoRepository;
import com.cooperativa.coop_servicios_backend.repositories.PerfilRepository;
import com.cooperativa.coop_servicios_backend.repositories.PermisoRepository;
import com.cooperativa.coop_servicios_backend.repositories.TipoIncidenteTecnicoRepository;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import com.cooperativa.coop_servicios_backend.services.ReclamoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

//Carga inicial unica
@Component
public class DataSeeder implements CommandLineRunner {

    //Catalogo de puestos de trabajo
    public static final String REDES = "Técnico de Redes y Fibra";
    public static final String TELEFONIA_TV = "Técnico de Telefonía y TV";
    public static final String ATENCION = "Atención al Cliente y Facturación";

    private static final List<String[]> ESPECIALIDADES = List.of(
            new String[]{REDES, "Internet, fibra óptica, routers y módems"},
            new String[]{TELEFONIA_TV, "Telefonía fija y televisión por cable"},
            new String[]{ATENCION, "Atención al cliente, consultas y facturación"}
    );


    private static final List<String> PERMISOS = List.of(
            "GESTIONAR_CLIENTES", "MODIFICAR_CLIENTES", "CREAR_CLIENTES",
            "ELIMINAR_CLIENTES", "VER_CLIENTES", "GESTIONAR_USUARIOS");

    private static final List<String> ESTADOS = List.of("Pendiente", "En proceso", "Resuelto");

    //Nombre, gravedad, especialidad
    private static final List<String[]> TIPOS_INCIDENTE = List.of(
            new String[]{"Sin conexión a internet", "Alta", REDES},
            new String[]{"Internet lento o intermitente", "Media", REDES},
            new String[]{"Router o módem con fallas", "Media", REDES},
            new String[]{"Sin señal de televisión", "Alta", TELEFONIA_TV},
            new String[]{"Imagen congelada o pixelada", "Media", TELEFONIA_TV},
            new String[]{"Línea telefónica sin tono", "Alta", TELEFONIA_TV},
            new String[]{"Ruidos o cortes en las llamadas", "Media", TELEFONIA_TV},
            new String[]{"Consulta o error de facturación", "Baja", ATENCION},
            new String[]{"Otro problema", "Baja", ATENCION}
    );
    private static final List<String> TABLAS_CON_SECUENCIA = List.of(
            "permisos", "perfiles", "usuarios", "localidades", "barrios",
            "especialidades", "estados", "tipos_incidente");

    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PermisoRepository permisoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private EstadoRepository estadoRepository;
    @Autowired private TipoIncidenteTecnicoRepository tipoRepository;
    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PlatformTransactionManager transactionManager;
    @PersistenceContext private EntityManager entityManager;

    @Override
    public void run(String... args) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        try {
            tx.executeWithoutResult(s -> entityManager.createNativeQuery(
                    "ALTER TABLE cambios_estado ALTER COLUMN empleado_id DROP NOT NULL").executeUpdate());
        } catch (Exception e) {
            System.err.println("[DataSeeder] No se pudo ajustar cambios_estado.empleado_id: " + e.getMessage());
        }

        tx.executeWithoutResult(s -> {
            sincronizarSecuencias();
            seedPerfiles();
            seedEspecialidades();
            seedEstados();
            seedTiposIncidente();
            seedUsuariosBase();
        });
    }


    //Deja la secuencia del id de cada tabla
    private void sincronizarSecuencias() {
        for (String tabla : TABLAS_CON_SECUENCIA) {
            entityManager.createNativeQuery(
                    "SELECT setval(pg_get_serial_sequence('" + tabla + "', 'id'), "
                            + "COALESCE((SELECT MAX(id) FROM " + tabla + "), 0) + 1, false)").getSingleResult();
        }
    }

    //perfiles
    private void seedPerfiles() {
        // Permisos (por nombre)
        List<Permiso> permisos = new ArrayList<>(permisoRepository.findAll());
        for (String nombre : PERMISOS) {
            if (permisos.stream().noneMatch(p -> nombre.equalsIgnoreCase(p.getNombre()))) {
                Permiso nuevo = new Permiso();
                nuevo.setNombre(nombre);
                permisos.add(permisoRepository.save(nuevo));
            }
        }

        // Administrador: debe tener todos los permisos del sistema
        Perfil admin = buscarPerfil(n -> n.equals("administrador"));
        if (admin == null) {
            admin = new Perfil();
            admin.setNombre("Administrador");
            admin.setPermisos(new ArrayList<>());
        }
        List<Permiso> delAdmin = admin.getPermisos() == null ? new ArrayList<>() : new ArrayList<>(admin.getPermisos());
        for (Permiso p : permisos) {
            if (PERMISOS.contains(p.getNombre()) && delAdmin.stream().noneMatch(x -> x.getId().equals(p.getId()))) {
                delAdmin.add(p);
            }
        }
        admin.setPermisos(delAdmin);
        perfilRepository.save(admin);

        //Cliente (ClienteService asigna el perfil con id 2 a los clientes nuevos) y Empleado
        if (buscarPerfil(n -> n.equals("cliente") || n.equals("role_cliente")) == null) {
            crearPerfil("Cliente");
        }
        if (buscarPerfil(n -> n.equals("empleado") || n.equals("role_empleado")) == null) {
            crearPerfil("Empleado");
        }
    }

    private Perfil buscarPerfil(Predicate<String> coincide) {
        return perfilRepository.findAll().stream()
                .filter(p -> coincide.test(ReclamoService.clave(p.getNombre())))
                .findFirst().orElse(null);
    }

    private void crearPerfil(String nombre) {
        Perfil perfil = new Perfil();
        perfil.setNombre(nombre);
        perfil.setPermisos(new ArrayList<>());
        perfilRepository.save(perfil);
    }


    //especialidades
    private void seedEspecialidades() {
        //El catalogo es exactamente esta lista: se eliminan los puestos que ya no son oficiales
        List<String> oficiales = ESPECIALIDADES.stream().map(o -> ReclamoService.clave(o[0])).toList();
        for (Especialidad e : especialidadRepository.findAll()) {
            if (!oficiales.contains(ReclamoService.clave(e.getNombre()))) {
                especialidadRepository.delete(e);
            }
        }

        for (String[] o : ESPECIALIDADES) {
            Especialidad e = especialidadRepository.findByNombre(o[0]).orElseGet(() -> new Especialidad(o[0], o[1]));
            if (e.getId() == null || e.getDescripcion() == null) {
                e.setDescripcion(o[1]);
                especialidadRepository.save(e);
            }
        }

        //Empleados: solo se corrige lo que se puede equiparar con certeza a un puesto oficial
        for (Empleado emp : empleadoRepository.findAll()) {
            String oficial = oficial(emp.getEspecialidad());
            if (oficial == null) {
                if (emp.getEspecialidad() != null && !emp.getEspecialidad().isBlank()) {
                    System.err.println("[DataSeeder] Empleado " + emp.getLegajo() + " tiene la especialidad '"
                            + emp.getEspecialidad() + "' que no está en el catálogo: editarlo y elegir un puesto.");
                }
            } else if (!oficial.equals(emp.getEspecialidad())) {
                emp.setEspecialidad(oficial);
                empleadoRepository.save(emp);
            }
        }
    }

    //Devuelve el nombre oficial del puesto si coincide con el catálogo, o null si no existe
    private String oficial(String texto) {
        String clave = ReclamoService.clave(texto);
        if (clave.isEmpty()) return null;
        for (String[] o : ESPECIALIDADES) if (ReclamoService.clave(o[0]).equals(clave)) return o[0];
        return null;
    }

    //estados
    private void seedEstados() {
        for (String nombre : ESTADOS) {
            if (estadoRepository.findFirstByNombreIgnoreCaseOrderByIdAsc(nombre).isEmpty()) {
                Estado estado = new Estado();
                estado.setNombre(nombre);
                estado.setDescripcion("Estado de reclamo: " + nombre);
                estadoRepository.save(estado);
            }
        }
    }

    //tipos de incidente
    private void seedTiposIncidente() {
        List<TipoIncidenteTecnico> existentes = tipoRepository.findAll();

        for (String[] t : TIPOS_INCIDENTE) {
            boolean existe = existentes.stream().anyMatch(x -> ReclamoService.clave(x.getNombre()).equals(ReclamoService.clave(t[0])));
            if (!existe) {
                TipoIncidenteTecnico tipo = new TipoIncidenteTecnico();
                tipo.setNombre(t[0]);
                tipo.setGravedad(t[1]);
                tipo.setEspecialidad(t[2]);
                tipoRepository.save(tipo);
            }
        }


        //Todo tipo apunta a un puesto oficial
        for (TipoIncidenteTecnico tipo : tipoRepository.findAll()) {
            String oficial = oficial(tipo.getEspecialidad());
            if (oficial == null) oficial = deducirEspecialidad(tipo.getNombre());
            if (!oficial.equals(tipo.getEspecialidad())) {
                tipo.setEspecialidad(oficial);
                tipoRepository.save(tipo);
            }
        }
    }

    //Asigna un puesto a un tipo de incidente segun palabras clave de su nombre
    static String deducirEspecialidad(String nombre) {
        String n = " " + ReclamoService.clave(nombre) + " ";
        if (contieneAlguna(n, "internet", "conexion", "wifi", "wi-fi", "router", "modem", "fibra", "velocidad", "lent", "intermit", " red ", " redes "))
            return REDES;
        if (contieneAlguna(n, "television", " tv ", "senal", "decodificador", "canal", "imagen", "cable", "telefon", "linea", "llamada", "tono"))
            return TELEFONIA_TV;
        return ATENCION;
    }

    private static boolean contieneAlguna(String texto, String... claves) {
        for (String c : claves) if (texto.contains(c)) return true;
        return false;
    }

    //usuarios base
    private void seedUsuariosBase() {
        if (usuarioRepository.findByEmail("admin@coop.com").isPresent()) return;

        Usuario admin = new Usuario();
        admin.setEmail("admin@coop.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setActivo(true);
        admin.setPerfil(buscarPerfil(n -> n.equals("administrador")));
        usuarioRepository.save(admin);

        System.out.println("==================================================");
        System.out.println("✅ USUARIO ADMIN CREADO");
        System.out.println("Email: admin@coop.com");
        System.out.println("Password: 123456");
        System.out.println("==================================================");
    }
}
