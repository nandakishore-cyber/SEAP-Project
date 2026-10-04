package com.alumni.portal.integration.repository;

import com.alumni.portal.entity.AlumniProfile;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.entity.enums.VerificationStatus;
import com.alumni.portal.repository.AlumniProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests for AlumniProfileRepository.
 * Tests custom JPQL queries for search and filtering.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AlumniProfileRepository Integration Tests")
class AlumniProfileRepositoryIntegrationTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private AlumniProfileRepository alumniProfileRepository;

    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = entityManager.persistAndFlush(User.builder()
                .firstName("Rahul").lastName("Verma")
                .email("rahul@college.edu").password("pass")
                .role(Role.ROLE_ALUMNI).enabled(true).build());

        user2 = entityManager.persistAndFlush(User.builder()
                .firstName("Sneha").lastName("Reddy")
                .email("sneha@college.edu").password("pass")
                .role(Role.ROLE_ALUMNI).enabled(true).build());

        user3 = entityManager.persistAndFlush(User.builder()
                .firstName("Kiran").lastName("Desai")
                .email("kiran@college.edu").password("pass")
                .role(Role.ROLE_ALUMNI).enabled(true).build());

        // Create alumni profiles
        entityManager.persistAndFlush(AlumniProfile.builder()
                .user(user1).collegeName("ABC College")
                .department("Computer Science").degree("B.Tech")
                .graduationYear(2020).rollNumber("CS001")
                .currentCompany("Google").currentDesignation("SDE")
                .industry("Tech").yearsOfExperience(4)
                .skills("Java, Python")
                .verificationStatus(VerificationStatus.VERIFIED).build());

        entityManager.persistAndFlush(AlumniProfile.builder()
                .user(user2).collegeName("ABC College")
                .department("Electronics").degree("B.Tech")
                .graduationYear(2021).rollNumber("EC001")
                .currentCompany("Amazon").currentDesignation("SDE-2")
                .industry("Tech").yearsOfExperience(3)
                .skills("C++, VLSI")
                .verificationStatus(VerificationStatus.VERIFIED).build());

        entityManager.persistAndFlush(AlumniProfile.builder()
                .user(user3).collegeName("ABC College")
                .department("Computer Science").degree("M.Tech")
                .graduationYear(2022).rollNumber("CS002")
                .currentCompany("Microsoft").currentDesignation("SDE")
                .industry("Tech").yearsOfExperience(2)
                .skills("Azure, Spring Boot")
                .verificationStatus(VerificationStatus.PENDING).build());
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("Should find alumni profile by user ID")
        void findByUserId_existingUser_returnsProfile() {
            Optional<AlumniProfile> result = alumniProfileRepository.findByUserId(user1.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getDepartment()).isEqualTo("Computer Science");
            assertThat(result.get().getCurrentCompany()).isEqualTo("Google");
        }

        @Test
        @DisplayName("Should return empty for non-existing user")
        void findByUserId_nonExistingUser_returnsEmpty() {
            Optional<AlumniProfile> result = alumniProfileRepository.findByUserId(999L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByVerificationStatus")
    class VerificationStatusTests {

        @Test
        @DisplayName("Should find verified profiles")
        void findByVerificationStatus_verified_returnsTwoProfiles() {
            List<AlumniProfile> verified = alumniProfileRepository
                    .findByVerificationStatus(VerificationStatus.VERIFIED);

            assertThat(verified).hasSize(2);
        }

        @Test
        @DisplayName("Should find pending profiles")
        void findByVerificationStatus_pending_returnsOneProfile() {
            List<AlumniProfile> pending = alumniProfileRepository
                    .findByVerificationStatus(VerificationStatus.PENDING);

            assertThat(pending).hasSize(1);
            assertThat(pending.get(0).getUser().getFirstName()).isEqualTo("Kiran");
        }
    }

    @Nested
    @DisplayName("searchAlumni (Custom JPQL)")
    class SearchAlumniTests {

        @Test
        @DisplayName("Should search by department")
        void searchAlumni_byDepartment_returnsMatchingVerifiedProfiles() {
            Page<AlumniProfile> results = alumniProfileRepository.searchAlumni(
                    "Computer Science", null, null, PageRequest.of(0, 10));

            // Only user1 is VERIFIED in CS (user3 is PENDING)
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getUser().getFirstName()).isEqualTo("Rahul");
        }

        @Test
        @DisplayName("Should search by graduation year")
        void searchAlumni_byGraduationYear_returnsMatching() {
            Page<AlumniProfile> results = alumniProfileRepository.searchAlumni(
                    null, 2021, null, PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getUser().getFirstName()).isEqualTo("Sneha");
        }

        @Test
        @DisplayName("Should search by company (partial match)")
        void searchAlumni_byCompanyPartial_returnsMatching() {
            Page<AlumniProfile> results = alumniProfileRepository.searchAlumni(
                    null, null, "goo", PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getCurrentCompany()).isEqualTo("Google");
        }

        @Test
        @DisplayName("Should return all verified when no filters applied")
        void searchAlumni_noFilters_returnsAllVerified() {
            Page<AlumniProfile> results = alumniProfileRepository.searchAlumni(
                    null, null, null, PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(2); // Only VERIFIED profiles
        }

        @Test
        @DisplayName("Should support pagination")
        void searchAlumni_withPagination_returnsPagedResults() {
            Page<AlumniProfile> page1 = alumniProfileRepository.searchAlumni(
                    null, null, null, PageRequest.of(0, 1));

            assertThat(page1.getContent()).hasSize(1);
            assertThat(page1.getTotalElements()).isEqualTo(2);
            assertThat(page1.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findByName")
    class FindByNameTests {

        @Test
        @DisplayName("Should find alumni by first name")
        void findByName_firstName_returnsMatch() {
            Page<AlumniProfile> results = alumniProfileRepository.findByName(
                    "Rahul", PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find alumni by partial last name")
        void findByName_partialLastName_returnsMatch() {
            Page<AlumniProfile> results = alumniProfileRepository.findByName(
                    "Red", PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getUser().getLastName()).isEqualTo("Reddy");
        }
    }
}
