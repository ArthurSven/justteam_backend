package com.devapps.just_team_backend.controller;

import com.devapps.just_team_backend.model.user.UserAuthRequest;
import com.devapps.just_team_backend.model.user.UserAuthResponse;
import com.devapps.just_team_backend.model.user.UserRequest;
import com.devapps.just_team_backend.model.user.UserResponse;
import com.devapps.just_team_backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

            // 2. Set HTTP-only cookie (HTTP concern)
            ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 3. Clear token from response body
            authResponse.setToken(null);
            return ResponseEntity.ok(authResponse);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(UserAuthResponse.builder()
                            .message(ex.getReason())
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        try {
            List<UserResponse> userListResponse = userService.getAllUsers();
            return ResponseEntity.ok(userListResponse);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(List.of(UserResponse.builder()
                            .response(ex.getReason())
                            .build()));
        }
    }
}
