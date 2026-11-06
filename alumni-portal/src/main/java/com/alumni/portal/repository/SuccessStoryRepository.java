package com.alumni.portal.repository;
import com.alumni.portal.entity.SuccessStory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SuccessStoryRepository extends JpaRepository<SuccessStory, Long> {
    List<SuccessStory> findByStatusOrderByCreatedAtDesc(String status);
    List<SuccessStory> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
