package com.devapps.just_team_backend.service;

import com.devapps.just_team_backend.model.user.*;
import com.devapps.just_team_backend.repository.UserRepository;
import com.devapps.just_team_backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserAuthResponse register(UserRequest userRequest) {
        String email = userRequest.getEmail();
        String username = userRequest.getUsername();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        } else  if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        try {
            var user = User.builder()
                    .firstname(userRequest.getFirstname())
                    .lastname(userRequest.getLastname())
                    .username(userRequest.getUsername())
                    .email(userRequest.getEmail())
                    .dob(userRequest.getDob())
                    .position(userRequest.getPosition())
                    .dept(userRequest.getDept())
                    .role(userRequest.getRole())
                    .startdate(userRequest.getStartdate())
                    .salary(userRequest.getSalary())
                    .password(passwordEncoder.encode(userRequest.getPassword()))
                    .build();

            if (user.getUsername().contains("\n") ||
                    user.getEmail().contains("\n") ||
                    user.getPassword().contains("\n")) {
                throw new IllegalArgumentException("Invalid input: Newline characters are not allowed.");
            }

            userRepository.save(user);
            var jwtToken = jwtService.createToken(user);
            return UserAuthResponse.builder()
                    .token(jwtToken)
                    .message(userRequest.getUsername() + "'s account has been created,")
                    .build();

        } catch (Exception e) {
            return UserAuthResponse.builder()
                    .token(null)
                    .message("The following error occurred: " + e.getLocalizedMessage())
                    .build();
        }
    }

    public UserAuthResponse login(UserAuthRequest userAuthRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userAuthRequest.getUsername(),
                            userAuthRequest.getPassword()
                    )
            );

            var user = (User) userRepository.findByUsername(userAuthRequest.getUsername()).orElseThrow();
            var jwtToken = jwtService.createToken(user);


            return UserAuthResponse.builder()
                    .username(userAuthRequest.getUsername())
                    .token(jwtToken)
                    .message("Welcome, " + userAuthRequest.getUsername())
                    .role(user.getRole())
                    .build();
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }
    }


    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Clear security context
            SecurityContextHolder.clearContext();


            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Clear cookies with matching attributes from login
            clearAuthCookies(response);

            // Add cache control headers
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Logged out successfully");

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Logout failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        // Clear JWT cookie - MUST match login attributes exactly
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .path("/")
                .httpOnly(true)
                .secure(true) // MUST match login setting!
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        ResponseCookie currentUserCookie = ResponseCookie.from("currentUser", "")
                .path("/")
                .httpOnly(false) // Angular sets this as regular cookie
                .secure(false) // Angular doesn't set secure flag
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, currentUserCookie.toString());

        // Clear other potential cookies
        List<String> otherCookies = Arrays.asList("JSESSIONID", "remember-me");
        otherCookies.forEach(cookieName -> {
            ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                    .path("/")
                    .httpOnly(true)
                    .secure(false)
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        });
    }

    @Cacheable(value = "usersCache", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @CacheEvict(value = "usersCache", allEntries = true)
    public void evictAllUsersCache() {
        System.out.println("Clearing users cache...");
    }
}
