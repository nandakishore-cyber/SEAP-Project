package com.alumni.portal.repository;

import com.alumni.portal.entity.ConnectionRequest;
import com.alumni.portal.entity.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    List<ConnectionRequest> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    Optional<ConnectionRequest> findByIdAndRecipientId(Long id, Long recipientId);

    boolean existsBySenderIdAndRecipientIdAndStatusIn(Long senderId, Long recipientId, Collection<ConnectionStatus> statuses);

    boolean existsBySenderIdAndRecipientIdAndStatus(Long senderId, Long recipientId, ConnectionStatus status);
}
