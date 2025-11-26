package org.example.userhandling.service;

import org.example.userhandling.dto.AssignUserRoleRequest;
import org.example.userhandling.dto.CreateUserRequest;
import org.example.userhandling.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final RestTemplate restTemplate;
    private final String keycloakUrl;
    private final String realm;
    private final String clientId;

    @Autowired
    public UserService(
            RestTemplate restTemplate,
            @Value("${keycloak.server-url}") String keycloakUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId) {
        this.restTemplate = restTemplate;
        this.keycloakUrl = keycloakUrl;
        this.realm = realm;
        this.clientId = clientId;

    }

    public String getClientUuid(String clientId, Jwt jwt) {
        String accessToken = jwt.getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                keycloakUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        if (!Objects.requireNonNull(response.getBody()).isEmpty()) {
            return (String) response.getBody().getFirst().get("id");
        }
        return null;
    }

    private List<String> getUserClientRoles(String userId, String clientUuid, Jwt jwt) {
        String accessToken = jwt.getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/clients/" + clientUuid,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> roles = response.getBody();
        if (roles == null) return List.of();

        return roles.stream()
                .map(role -> (String) role.get("name"))
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers(Jwt jwt) {
        String accessToken = jwt.getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                keycloakUrl + "/admin/realms/" + realm + "/users",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> users = response.getBody();

        String clientUuid = getClientUuid(clientId, jwt);

        return Objects.requireNonNull(users).stream()
                .map(user -> {
                    String userId = (String) user.get("id");
                    List<String> userRoles = getUserClientRoles(userId, clientUuid, jwt);
                    return new UserDTO(
                            userId,
                            (String) user.get("username"),
                            (String) user.get("firstName"),
                            (String) user.get("lastName"),
                            (String) user.get("email"),
                            userRoles
                    );

                })
                .collect(Collectors.toList());

    }

    public String createUser(CreateUserRequest createUserRequest, Jwt jwt) {
        String accessToken = jwt.getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "enabled", true,
                "username", createUserRequest.getUsername(),
                "firstName", createUserRequest.getFirstName(),
                "lastName", createUserRequest.getLastName(),
                "email", createUserRequest.getEmail(),
                "emailVerified", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", createUserRequest.getPassword(),
                        "temporary", false
                ))
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                keycloakUrl + "/admin/realms/" + realm + "/users/",
                request, Void.class
        );

        URI location = response.getHeaders().getLocation();
        if(location == null) {
            throw new RuntimeException("No location header found, User creation failed");
        }
        String[] parts = location.getPath().split("/");
        return parts[parts.length - 1];
    }

    public void assignUserRole(AssignUserRoleRequest userRoleRequest, Jwt jwt) {
        String accessToken = jwt.getTokenValue();

        String url = String.format(
                "%s/admin/realms/%s/users/%s/role-mappings/clients/%s",
                keycloakUrl, realm, userRoleRequest.getUserId(), userRoleRequest.getClientUuid()
        );

        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("id", userRoleRequest.getRoleId());
        roleMap.put("name", userRoleRequest.getRoleName());

        List<Map<String, Object>> body = List.of(roleMap);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

    }

    public void deleteUser(String userId, Jwt jwt){
        String accessToken = jwt.getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                keycloakUrl + "/admin/realms/" + realm + "/users/" + userId,
                HttpMethod.DELETE, request, Void.class

        );

    }
}
