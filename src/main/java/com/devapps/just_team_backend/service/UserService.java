package com.devapps.just_team_backend.service;

import com.devapps.just_team_backend.model.user.*;
import com.devapps.just_team_backend.repository.UserRepository;
import com.devapps.just_team_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

            ResponseCookie responseCookie = ResponseCookie.from("jwt", jwtToken).build();
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

    @Cacheable(value = "usersCache")
    public List<UserResponse> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            return users.stream()
                    .map(user -> UserResponse.builder()
                            .userid(user.getUserid())
                            .username(user.getUsername())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .dob(user.getDob())
                            .role(user.getRole())
                            .position(user.getPosition())
                            .startdate(user.getStartdate())
                            .dept(user.getDept())
                            .build())
                    .toList();
        } catch (ResponseStatusException rse) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,  "A problem occurred: " + rse.getLocalizedMessage());
        }
    }

    @CacheEvict(value = "usersCache", allEntries = true)
    public void evictAllUsersCache() {
        System.out.println("Clearing users cache...");
    }
}
