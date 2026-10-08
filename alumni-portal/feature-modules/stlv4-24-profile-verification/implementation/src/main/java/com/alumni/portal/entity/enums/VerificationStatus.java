package com.alumni.portal.entity.enums;

/**
 * Tracks the verification lifecycle of an alumni profile.
 * PENDING  - Awaiting admin review
 * VERIFIED - Confirmed by admin
 * REJECTED - Rejected by admin (reason stored separately)
 */
public enum VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
