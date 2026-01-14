package org.reserv.reserveme.auth;

public record RegisterRequest(String email, String password, String displayName) {}
