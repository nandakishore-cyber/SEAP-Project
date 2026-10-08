package com.alumni.portal.repository;

import com.alumni.portal.entity.AlumniProfile;
import com.alumni.portal.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {

    Optional<AlumniProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<AlumniProfile> findByVerificationStatus(VerificationStatus status);

    Page<AlumniProfile> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    @Query("SELECT a FROM AlumniProfile a WHERE " +
           "(:department IS NULL OR a.department = :department) AND " +
           "(:graduationYear IS NULL OR a.graduationYear = :graduationYear) AND " +
           "(:company IS NULL OR LOWER(a.currentCompany) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "a.verificationStatus = 'VERIFIED'")
    Page<AlumniProfile> searchAlumni(
            @Param("department") String department,
            @Param("graduationYear") Integer graduationYear,
            @Param("company") String company,
            Pageable pageable);

    @Query("SELECT a FROM AlumniProfile a JOIN a.user u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<AlumniProfile> findByName(@Param("name") String name, Pageable pageable);
}
