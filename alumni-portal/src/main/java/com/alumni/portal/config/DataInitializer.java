package com.alumni.portal.config;

import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the database with a default admin user for development.
 * Only active when the "dev" or default profile is active (NOT in "test" or "prod").
 */
@Configuration
@Profile("!test & !prod")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@alumni-portal.com")) {
                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email("admin@alumni-portal.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ROLE_ADMIN)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                logger.info("✅ Default admin user created: admin@alumni-portal.com / Admin@123");
            }

            if (!userRepository.existsByEmail("alumni@demo.com")) {
                User demoAlumni = User.builder()
                        .firstName("Demo")
                        .lastName("Alumni")
                        .email("alumni@demo.com")
                        .password(passwordEncoder.encode("Alumni@123"))
                        .role(Role.ROLE_ALUMNI)
                        .enabled(true)
                        .build();
                userRepository.save(demoAlumni);
                logger.info("✅ Demo alumni user created: alumni@demo.com / Alumni@123");
            }

            logger.info("📊 Total users in database: {}", userRepository.count());
        };
    }
}
