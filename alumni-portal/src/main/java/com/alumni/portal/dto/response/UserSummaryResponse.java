package com.alumni.portal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private Long id;
    private String name;
    private String email;
}
