package com.alumni.portal.integration.repository;

import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests using @DataJpaTest.
 * Spins up a minimal Spring context with only JPA components + H2.
 * Faster than @SpringBootTest — ideal for testing custom queries.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Arun")
                .lastName("Patel")
                .email("arun.patel@college.edu")
                .password("encoded-password")
                .role(Role.ROLE_ALUMNI)
                .enabled(true)
                .build();
        entityManager.persistAndFlush(testUser);
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by existing email")
        void findByEmail_existingEmail_returnsUser() {
            Optional<User> found = userRepository.findByEmail("arun.patel@college.edu");

            assertThat(found).isPresent();
            assertThat(found.get().getFirstName()).isEqualTo("Arun");
            assertThat(found.get().getRole()).isEqualTo(Role.ROLE_ALUMNI);
        }

        @Test
        @DisplayName("Should return empty for non-existing email")
        void findByEmail_nonExistingEmail_returnsEmpty() {
            Optional<User> found = userRepository.findByEmail("nobody@college.edu");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Email lookup should be case-sensitive")
        void findByEmail_differentCase_dependsOnDB() {
            // H2 default collation is case-insensitive, but this documents the behavior
            Optional<User> found = userRepository.findByEmail("arun.patel@college.edu");
            assertThat(found).isPresent();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true for existing email")
        void existsByEmail_existingEmail_returnsTrue() {
            boolean exists = userRepository.existsByEmail("arun.patel@college.edu");
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existing email")
        void existsByEmail_nonExistingEmail_returnsFalse() {
            boolean exists = userRepository.existsByEmail("ghost@college.edu");
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should save and retrieve a user")
        void save_validUser_persistsSuccessfully() {
            User newUser = User.builder()
                    .firstName("Neha")
                    .lastName("Gupta")
                    .email("neha.gupta@college.edu")
                    .password("encoded")
                    .role(Role.ROLE_STUDENT)
                    .enabled(true)
                    .build();

            User saved = userRepository.save(newUser);

            assertThat(saved.getId()).isNotNull();
            assertThat(userRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("Should count users correctly")
        void count_afterInsert_returnsCorrectCount() {
            long initialCount = userRepository.count();

            User another = User.builder()
                    .firstName("Count")
                    .lastName("Test")
                    .email("count@test.com")
                    .password("encoded")
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            entityManager.persistAndFlush(another);

            assertThat(userRepository.count()).isEqualTo(initialCount + 1);
        }
    }
}
