package com.devapps.just_team_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeamController {

    @GetMapping("/")
    public ResponseEntity<String> getJutTeamApplication() {
        return ResponseEntity.ok(
                "Just Team is running"
        );
    }
}
