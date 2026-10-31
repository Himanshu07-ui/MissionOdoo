package com.brightloop.billing.dto;

import com.brightloop.billing.entity.User;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private User.Role role = User.Role.CUSTOMER;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
}
