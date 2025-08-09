package com.devapps.just_team_backend.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID userid;
    private String firstname;
    private String lastname;

    private String username;

    private String email;
    private String password;
    private String role;
    private String dob;
    private String startdate;
    private String position;
    private String dept;
    private String salary;
    private String response;
}
