package org.example.userhandling.service;

import org.example.userhandling.dto.AssignUserRoleRequest;
import org.example.userhandling.dto.CreateUserRequest;
import org.example.userhandling.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Jwt jwt;

    private UserService userService;

    private final String keycloakUrl = "http://keycloak:8080";
    private final String realm = "test-realm";
    private final String clientId = "test-client";

    @BeforeEach
    void setUp() {
        userService = new UserService(restTemplate, keycloakUrl, realm, clientId);

        lenient().when(jwt.getTokenValue()).thenReturn("mock-token");
    }

    @Test
    void getClientUuid_ReturnsClientUuid_WhenClientExists() {
        String expectedClientUuid = "client-uuid-123";
        List<Map<String, Object>> clients = List.of(
                Map.of("id", expectedClientUuid, "clientId", clientId)
        );

        ResponseEntity<List<Map<String, Object>>> response =
                new ResponseEntity<>(clients, HttpStatus.OK);

        lenient().when(restTemplate.exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        String result = userService.getClientUuid(clientId, jwt);

        assertEquals(expectedClientUuid, result);
        verify(restTemplate).exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class));

    }

    @Test
    void getClientUuid_ReturnsNull_WhenClientDoesNotExist() {
        List<Map<String, Object>> emptyClient = List.of();
        ResponseEntity<List<Map<String, Object>>> response =
                new ResponseEntity<>(emptyClient, HttpStatus.OK);
        lenient().when(restTemplate.exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        String result = userService.getClientUuid(clientId, jwt);

        assertNull(result);
        verify(restTemplate).exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllUsers_ReturnsUsers(){
        List<Map<String, Object>> users = List.of(
                Map.of("id", "user1", "username", "mockuser", "firstName",
                        "John", "lastName", "Doe")
        );

        ResponseEntity<List<Map<String, Object>>> response = new ResponseEntity<>(users, HttpStatus.OK);

        lenient().when(restTemplate.exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/users"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        lenient().when(restTemplate.exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(
                List.of(Map.of("id", "client-123")),
                HttpStatus.OK
        ));

        lenient().when(restTemplate.exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/users/user1/role-mappings/clients/client-123"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(
                List.of(Map.of("name", "admin")),
                HttpStatus.OK
        ));

        List<UserDTO> result = userService.getAllUsers(jwt);

        assertEquals(1, result.size());
        assertEquals("user1", result.getFirst().getId());
        assertEquals(List.of("admin"), result.getFirst().getClientRoles());

    }

    @Test
    void createUser_ReturnsNewUser(){
        CreateUserRequest createUserRequest = new CreateUserRequest("john", "John", "Doe",
                "john@mock.com", "password");

        URI location = URI.create("/admin/realms/" + realm + "/users/new-user-id");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        ResponseEntity<Void> response = new ResponseEntity<>(headers, HttpStatus.CREATED);

        lenient().when(restTemplate.postForEntity(
                eq(keycloakUrl + "/admin/realms/" + realm + "/users/"),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(response);

        String result = userService.createUser(createUserRequest, jwt);
        assertEquals("new-user-id", result);
    }

    @Test
    void createUser_ThrowsException_WhenLocationIsNull() {
        CreateUserRequest createUserRequest = new CreateUserRequest("john", "John", "Doe",
                "john@mock.com", "password");

        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Void> response = new ResponseEntity<>(headers, HttpStatus.CREATED);

        lenient().when(restTemplate.postForEntity(
                eq(keycloakUrl + "/admin/realms/" + realm + "/users/"),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(response);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(createUserRequest, jwt));

        assertEquals("No location header found, User creation failed", exception.getMessage());

    }

    @Test
    void assignUserRole_SendsPostRequest(){
        AssignUserRoleRequest assignUserRoleRequest = new AssignUserRoleRequest("user1", "client-123",
                "role-id-1", "admin");

        lenient().when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.ok().build());

        userService.assignUserRole(assignUserRoleRequest, jwt);

        verify(restTemplate).exchange(
                contains("/users/user1/role-mappings/clients/client-123"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void deleteUser_SendsDeleteRequest(){
        lenient().when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.ok().build());

        userService.deleteUser("user1", jwt);

        verify(restTemplate).exchange(
                eq(keycloakUrl + "/admin/realms/" + realm + "/users/user1"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }
}