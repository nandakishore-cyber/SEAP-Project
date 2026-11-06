package com.alumni.portal.service;

import com.alumni.portal.dto.request.SuccessStoryRequest;
import com.alumni.portal.dto.response.SuccessStoryResponse;
import com.alumni.portal.entity.*;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SuccessStoryService {
    private final SuccessStoryRepository stories; private final UserRepository users;
    public SuccessStoryService(SuccessStoryRepository stories, UserRepository users) { this.stories=stories; this.users=users; }
    @Transactional(readOnly = true) public List<SuccessStoryResponse> published() { return stories.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream().map(this::map).toList(); }
    @Transactional(readOnly = true) public List<SuccessStoryResponse> pending() { return stories.findByStatusOrderByCreatedAtDesc("PENDING").stream().map(this::map).toList(); }
    @Transactional(readOnly = true) public List<SuccessStoryResponse> mine(Long userId) { return stories.findByAuthorIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList(); }
    @Transactional public SuccessStoryResponse create(Long userId, SuccessStoryRequest request) {
        User author=users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (author.getRole()!=Role.ROLE_ALUMNI) throw new IllegalArgumentException("Only alumni can submit success stories");
        SuccessStory story=SuccessStory.builder().author(author).title(request.getTitle().trim()).content(request.getContent().trim()).achievement(request.getAchievement()).status("PENDING").build();
        return map(stories.save(story));
    }
    @Transactional public SuccessStoryResponse update(Long userId, Long storyId, SuccessStoryRequest request) {
        SuccessStory story=stories.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("SuccessStory", "id", storyId));
        if (!story.getAuthor().getId().equals(userId) || !"PENDING".equals(story.getStatus()))
            throw new ResourceNotFoundException("SuccessStory", "id", storyId);
        story.setTitle(request.getTitle().trim());
        story.setContent(request.getContent().trim());
        story.setAchievement(request.getAchievement());
        return map(stories.save(story));
    }
    @Transactional public SuccessStoryResponse moderate(Long id, String status) {
        if (!List.of("PUBLISHED", "REJECTED").contains(status)) throw new IllegalArgumentException("Status must be PUBLISHED or REJECTED");
        SuccessStory story=stories.findById(id).orElseThrow(() -> new ResourceNotFoundException("SuccessStory", "id", id));
        story.setStatus(status); return map(stories.save(story));
    }
    @Transactional public void delete(Long id, Long userId, boolean admin) {
        SuccessStory story=stories.findById(id).orElseThrow(() -> new ResourceNotFoundException("SuccessStory", "id", id));
        if (!admin && !story.getAuthor().getId().equals(userId)) throw new ResourceNotFoundException("SuccessStory", "id", id);
        stories.delete(story);
    }
    private SuccessStoryResponse map(SuccessStory s) { User u=s.getAuthor(); return new SuccessStoryResponse(s.getId(),u.getId(),u.getFirstName()+" "+u.getLastName(),s.getTitle(),s.getContent(),s.getAchievement(),s.getStatus(),s.getCreatedAt()); }
}
