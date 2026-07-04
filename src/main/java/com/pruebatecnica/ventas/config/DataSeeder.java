package com.pruebatecnica.ventas.config;

import com.pruebatecnica.ventas.domain.Usuario;
import com.pruebatecnica.ventas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * El ejercicio no exige un CRUD de usuarios, solo un usuario simple para
 * autenticarse. Este runner crea un usuario "admin" en el primer arranque
 * si la tabla de usuarios está vacía, para que el login funcione de
 * inmediato sin pasos manuales adicionales.
 *
 * Credenciales por defecto: admin / admin123 (ver README para más detalle).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol("ROLE_ADMIN");
            admin.setActivo(true);
            usuarioRepository.save(admin);
            logger.info("Usuario administrador inicial creado (username: admin)");
        }
    }
}
