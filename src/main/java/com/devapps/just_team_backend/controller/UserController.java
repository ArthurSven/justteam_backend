package com.devapps.just_team_backend.controller;

import com.devapps.just_team_backend.model.user.*;
import com.devapps.just_team_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserAuthResponse> register(@RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode())
                    .body(UserAuthResponse.builder()
                            .token(null)
                            .message(rse.getReason())
                            .build());
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<UserAuthResponse> login(
            @RequestBody UserAuthRequest request,
            HttpServletResponse response) {
        try {

            UserAuthResponse authResponse = userService.login(request);

            // Create JWT cookie with consistent attributes
            ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                    .httpOnly(true)
                    .secure(false) // Set to false for development (http://localhost)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 days
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Clear token from response body (security best practice)
            authResponse.setToken(null);

            return ResponseEntity.ok(authResponse);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(UserAuthResponse.builder()
                            .message(ex.getReason())
                            .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        return userService.logout(request, response);
    }

    @GetMapping("/corporate-users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/auth-user")
    public ResponseEntity<UserAuthResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        // The @AuthenticationPrincipal injects the user from the JWT token
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAuthResponse response = UserAuthResponse.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .message("Authenticated user")
                .build();

        return ResponseEntity.ok(response);

    }

}
