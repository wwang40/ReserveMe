package org.reserv.reserveme.user.dto;

public record CreateUserRequest(
        String email,
        String displayName
) {}

