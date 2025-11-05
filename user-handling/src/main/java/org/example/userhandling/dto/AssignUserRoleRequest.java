package org.example.userhandling.dto;

public class AssignUserRoleRequest {
    private String userId;
    private String clientUuid;
    private String roleId;
    private String roleName;

    public AssignUserRoleRequest() {}

    public AssignUserRoleRequest(String userId, String clientUuid, String roleId, String roleName) {
        this.userId = userId;
        this.clientUuid = clientUuid;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientUuid() {
        return clientUuid;
    }

    public void setClientUuid(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
