package org.example.userhandling.controller;


import org.example.userhandling.dto.AssignUserRoleRequest;
import org.example.userhandling.dto.CreateUserRequest;
import org.example.userhandling.dto.UserDTO;
import org.example.userhandling.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users/get-all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @PostMapping("/users/create-user")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest createUserRequest) {
        String userId = userService.createUser(createUserRequest);
        return ResponseEntity.ok("User with id:" + userId + " created.");
    }

    @PostMapping("/admin/users/assign-role")
    public ResponseEntity<String> assignRoleToUser(@RequestBody AssignUserRoleRequest assignUserRoleRequest) {
        userService.assignUserRole(assignUserRoleRequest);
        return ResponseEntity.ok("Role assigned.");
    }

    @DeleteMapping("/admin/users/delete-user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
