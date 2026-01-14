package org.reserv.reserveme.auth;

import org.reserv.reserveme.user.User;
import org.reserv.reserveme.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegisterRequest req) {
        // pass displayName through to user service
        User user = userService.registerUser(req.email(), req.password(), req.displayName());
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, refresh);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        User user = userService.authenticate(req.email(), req.password());
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, refresh);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest req) {
        var tokenId = java.util.UUID.fromString(req.refreshToken());
        var maybe = jwtService.findRefreshToken(tokenId);
        if (maybe.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        var stored = maybe.get();
        if (stored.getExpiresAt().isBefore(java.time.Instant.now())) {
            jwtService.deleteRefreshToken(stored);
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        var user = stored.getUser();
        String access = jwtService.generateAccessToken(user);
        // Optionally rotate refresh token: delete stored and issue new one
        jwtService.deleteRefreshToken(stored);
        String newRefresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, newRefresh);
    }
}
