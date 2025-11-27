package org.example.userhandling.controller;


import org.example.userhandling.dto.AssignUserRoleRequest;
import org.example.userhandling.dto.CreateUserRequest;
import org.example.userhandling.dto.UserDTO;
import org.example.userhandling.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/userhandling")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users/get-all")
    public ResponseEntity<List<UserDTO>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(this.userService.getAllUsers(jwt));
    }

    @PostMapping("/users/create-user")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest createUserRequest,
                                             @AuthenticationPrincipal Jwt jwt) {
        String userId = userService.createUser(createUserRequest, jwt);
        return ResponseEntity.ok("User with id:" + userId + " created.");
    }

    @PostMapping("/admin/users/assign-role")
    public ResponseEntity<String> assignRoleToUser(@RequestBody AssignUserRoleRequest assignUserRoleRequest,
                                                   @AuthenticationPrincipal Jwt jwt) {
        userService.assignUserRole(assignUserRoleRequest, jwt);
        return ResponseEntity.ok("Role assigned.");
    }

    @DeleteMapping("/admin/users/delete-user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(id, jwt);
        return ResponseEntity.noContent().build();
    }

}
