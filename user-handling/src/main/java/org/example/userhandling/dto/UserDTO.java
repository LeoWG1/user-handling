package org.example.userhandling.dto;

import java.util.List;

public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> clientRoles;

    public UserDTO() {}

    public UserDTO(String id, String username, String firstName, String lastName,
                   String email, List<String> clientRoles) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.clientRoles = clientRoles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getClientRoles() {
        return clientRoles;
    }

    public void setClientRoles(List<String> clientRoles) {
        this.clientRoles = clientRoles;
    }
}
