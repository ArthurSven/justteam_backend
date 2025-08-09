package com.devapps.just_team_backend.config;

import com.devapps.just_team_backend.model.user.User;
import com.devapps.just_team_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@justteam.com").isEmpty()) {
            var admin = User.builder()
                    .email("admin@justteam.com")
                    .username("JustTeam Admin")
                    .password(passwordEncoder.encode("just_team"))
                    .role("Admin")
                    .firstname("JustTeam")
                    .lastname("Admin")
                    // Optional fields, set to null or default values
                    .dob(null)
                    .startdate(null)
                    .position(null)
                    .dept(null)
                    .salary(null)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin account created successfully");
        } else {
            System.out.println("Admin account already exists");
        }
    }
}
