package com.alumni.portal.unit.service;

import com.alumni.portal.dto.response.ConnectionRequestResponse;
import com.alumni.portal.entity.ConnectionRequest;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.ConnectionStatus;
import com.alumni.portal.entity.enums.NotificationType;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.ConnectionRequestRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.ConnectionRequestNotificationDispatcher;
import com.alumni.portal.service.ConnectionRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionRequestServiceTest {

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConnectionRequestNotificationDispatcher notificationDispatcher;

    @InjectMocks
    private ConnectionRequestService connectionRequestService;

    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Brown")
                .email("alice@test.com")
                .password("encoded")
                .role(com.alumni.portal.entity.enums.Role.ROLE_ALUMNI)
                .enabled(true)
                .build();

        recipient = User.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("White")
                .email("bob@test.com")
                .password("encoded")
                .role(com.alumni.portal.entity.enums.Role.ROLE_ALUMNI)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("sendRequest should create a pending connection request")
    void sendRequest_validRequest_createsPendingRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(2L), eq(1L), any())).thenReturn(false);

        ConnectionRequest savedRequest = ConnectionRequest.builder()
                .id(10L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenReturn(savedRequest);
        when(notificationDispatcher.createNotification(eq(2L), eq(NotificationType.NEW_CONNECTION_REQUEST), anyString(), anyString()))
                .thenReturn(null);

        ConnectionRequestResponse response = connectionRequestService.sendRequest(1L, 2L);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getRecipientId()).isEqualTo(2L);
        verify(connectionRequestRepository).save(any(ConnectionRequest.class));
    }

    @Test
    @DisplayName("sendRequest should reject invalid recipient")
    void sendRequest_invalidRecipient_throwsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectionRequestService.sendRequest(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("sendRequest should reject self requests")
    void sendRequest_selfRequest_throwsIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> connectionRequestService.sendRequest(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    @DisplayName("sendRequest should reject duplicate pending request")
    void sendRequest_duplicatePendingRequest_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(true);

        assertThatThrownBy(() -> connectionRequestService.sendRequest(1L, 2L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("sendRequest should reject reverse duplicate pending request")
    void sendRequest_reverseDuplicatePendingRequest_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(2L), eq(1L), any())).thenReturn(true);

        assertThatThrownBy(() -> connectionRequestService.sendRequest(1L, 2L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("getSentRequests should return requests for the authenticated user")
    void getSentRequests_returnsUsersRequests() {
        when(userRepository.existsById(1L)).thenReturn(true);
        ConnectionRequest request = ConnectionRequest.builder()
                .id(10L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(connectionRequestRepository.findBySenderIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(request));

        List<ConnectionRequestResponse> responses = connectionRequestService.getSentRequests(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSenderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getReceivedRequests should return requests for the authenticated user")
    void getReceivedRequests_returnsUsersRequests() {
        when(userRepository.existsById(2L)).thenReturn(true);
        ConnectionRequest request = ConnectionRequest.builder()
                .id(11L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(connectionRequestRepository.findByRecipientIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(request));

        List<ConnectionRequestResponse> responses = connectionRequestService.getReceivedRequests(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRecipientId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("acceptRequest should only allow recipient to accept")
    void acceptRequest_notRecipient_throwsAccessDenied() {
        ConnectionRequest request = ConnectionRequest.builder()
                .id(15L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build();

        when(connectionRequestRepository.findById(15L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> connectionRequestService.acceptRequest(99L, 15L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("rejectRequest should mark request as rejected")
    void rejectRequest_validRecipient_marksRejected() {
        ConnectionRequest request = ConnectionRequest.builder()
                .id(16L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(connectionRequestRepository.findById(16L)).thenReturn(Optional.of(request));
        when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationDispatcher.createNotification(eq(1L), eq(NotificationType.CONNECTION_REQUEST_REJECTED), anyString(), anyString()))
                .thenReturn(null);

        ConnectionRequestResponse response = connectionRequestService.rejectRequest(2L, 16L);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.REJECTED);
    }

    @Test
    @DisplayName("acceptRequest should mark request as accepted")
    void acceptRequest_validRecipient_marksAccepted() {
        ConnectionRequest request = ConnectionRequest.builder()
                .id(20L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(connectionRequestRepository.findById(20L)).thenReturn(Optional.of(request));
        when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationDispatcher.createNotification(eq(1L), eq(NotificationType.CONNECTION_REQUEST_ACCEPTED), anyString(), anyString()))
                .thenReturn(null);

        ConnectionRequestResponse response = connectionRequestService.acceptRequest(2L, 20L);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.ACCEPTED);
    }

    @Test
    @DisplayName("sender cannot accept request")
    void senderCannotAcceptRequest() {
        ConnectionRequest request = ConnectionRequest.builder()
                .id(21L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build();

        when(connectionRequestRepository.findById(21L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> connectionRequestService.acceptRequest(1L, 21L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("already processed request cannot be processed again")
    void processedRequest_cannotBeProcessedAgain() {
        ConnectionRequest request = ConnectionRequest.builder()
                .id(22L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.ACCEPTED)
                .build();

        when(connectionRequestRepository.findById(22L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> connectionRequestService.rejectRequest(2L, 22L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("accepted connection prevents duplicate request")
    void acceptedConnection_preventsDuplicateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(true);

        assertThatThrownBy(() -> connectionRequestService.sendRequest(1L, 2L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("rejected request can be retried")
    void rejectedRequest_canBeRetried() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndRecipientIdAndStatusIn(eq(2L), eq(1L), any())).thenReturn(false);

        ConnectionRequest savedRequest = ConnectionRequest.builder()
                .id(23L)
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build();
        when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenReturn(savedRequest);
        when(notificationDispatcher.createNotification(eq(2L), eq(NotificationType.NEW_CONNECTION_REQUEST), anyString(), anyString()))
                .thenReturn(null);

        ConnectionRequestResponse response = connectionRequestService.sendRequest(1L, 2L);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.PENDING);
    }
}
