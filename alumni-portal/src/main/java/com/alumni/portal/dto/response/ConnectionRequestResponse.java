package com.alumni.portal.dto.response;

import com.alumni.portal.entity.enums.ConnectionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConnectionRequestResponse {

    private Long id;
    private Long senderId;
    private Long recipientId;
    private String senderFirstName;
    private String senderLastName;
    private String recipientFirstName;
    private String recipientLastName;
    private ConnectionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
