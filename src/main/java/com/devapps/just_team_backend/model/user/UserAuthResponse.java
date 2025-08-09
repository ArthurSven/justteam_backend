package com.devapps.just_team_backend.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthResponse {
    private String token;
    private String username;
    private String message;
    private String role;
}
