package com.devapps.just_team_backend.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User implements UserDetails {

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userid;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private String email;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String role;

    @Column
    private String dob;

    @Column
    private String startdate;

    @Column
    private String position;

    @Column
    private String dept;

    public UUID getUserid() {
        return userid;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getDob() {
        return dob;
    }

    public String getStartdate() {
        return startdate;
    }

    public String getPosition() {
        return position;
    }

    public String getDept() {
        return dept;
    }

    public String getSalary() {
        return salary;
    }

    @Column
    private String salary;

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role)
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
