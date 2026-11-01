package com.alumni.portal.repository;

import com.alumni.portal.entity.AlumniEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlumniEventRepository extends JpaRepository<AlumniEvent, Long> {
    List<AlumniEvent> findByEventDateAfterOrderByEventDateAsc(LocalDateTime from);
}
