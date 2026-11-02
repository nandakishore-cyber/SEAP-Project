package com.alumni.portal.service;

import com.alumni.portal.dto.response.ConnectionRequestResponse;
import com.alumni.portal.entity.ConnectionRequest;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.ConnectionStatus;
import com.alumni.portal.entity.enums.NotificationType;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.ConnectionRequestRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConnectionRequestService {

    private final ConnectionRequestRepository connectionRequestRepository;
    private final UserRepository userRepository;
    private final ConnectionRequestNotificationDispatcher notificationDispatcher;

    public ConnectionRequestService(ConnectionRequestRepository connectionRequestRepository,
                                   UserRepository userRepository,
                                   ConnectionRequestNotificationDispatcher notificationDispatcher) {
        this.connectionRequestRepository = connectionRequestRepository;
        this.userRepository = userRepository;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Transactional
    public ConnectionRequestResponse sendRequest(Long senderId, Long recipientId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recipientId));

        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("You cannot send a connection request to yourself");
        }

        boolean hasExistingRelation =
                connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(
                        senderId, recipientId, List.of(ConnectionStatus.PENDING, ConnectionStatus.ACCEPTED))
                        || connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(
                        recipientId, senderId, List.of(ConnectionStatus.PENDING, ConnectionStatus.ACCEPTED));

        if (hasExistingRelation) {
            throw new DuplicateResourceException(
                    "Connection request already exists for these users",
                    "senderId/recipientId",
                    senderId + " and " + recipientId);
        }

        ConnectionRequest request = ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build();

        ConnectionRequest saved = connectionRequestRepository.save(request);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        notificationDispatcher.createNotification(
                recipientId,
                NotificationType.NEW_CONNECTION_REQUEST,
                "New connection request",
                senderName + " sent you a connection request.");

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getSentRequests(Long userId) {
        validateUser(userId);
        return connectionRequestRepository.findBySenderIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getReceivedRequests(Long userId) {
        validateUser(userId);
        return connectionRequestRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ConnectionRequestResponse acceptRequest(Long currentUserId, Long requestId) {
        return processRequest(currentUserId, requestId, ConnectionStatus.ACCEPTED);
    }

    @Transactional
    public ConnectionRequestResponse rejectRequest(Long currentUserId, Long requestId) {
        return processRequest(currentUserId, requestId, ConnectionStatus.REJECTED);
    }

    private ConnectionRequestResponse processRequest(Long currentUserId, Long requestId, ConnectionStatus newStatus) {
        ConnectionRequest request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request", "id", requestId));

        if (!request.getRecipient().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the recipient can process this connection request");
        }

        if (request.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING requests can be processed");
        }

        request.setStatus(newStatus);
        ConnectionRequest updated = connectionRequestRepository.save(request);

        String recipientName = request.getRecipient().getFirstName() + " " + request.getRecipient().getLastName();
        String message = newStatus == ConnectionStatus.ACCEPTED
                ? "Your connection request was accepted."
                : "Your connection request was rejected.";

        notificationDispatcher.createNotification(
                request.getSender().getId(),
                newStatus == ConnectionStatus.ACCEPTED
                        ? NotificationType.CONNECTION_REQUEST_ACCEPTED
                        : NotificationType.CONNECTION_REQUEST_REJECTED,
                newStatus == ConnectionStatus.ACCEPTED ? "Connection request accepted" : "Connection request rejected",
                message);

        return mapToResponse(updated);
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
    }

    private ConnectionRequestResponse mapToResponse(ConnectionRequest connectionRequest) {
        User sender = connectionRequest.getSender();
        User recipient = connectionRequest.getRecipient();

        return ConnectionRequestResponse.builder()
                .id(connectionRequest.getId())
                .senderId(sender.getId())
                .recipientId(recipient.getId())
                .senderFirstName(sender.getFirstName())
                .senderLastName(sender.getLastName())
                .recipientFirstName(recipient.getFirstName())
                .recipientLastName(recipient.getLastName())
                .status(connectionRequest.getStatus())
                .createdAt(connectionRequest.getCreatedAt())
                .updatedAt(connectionRequest.getUpdatedAt())
                .build();
    }
}
