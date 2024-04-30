package org.company.collect

class Group {
    private String group;
    private Role[] roles;

    String getGroup() { return group; }
    void setGroup(String value) { this.group = value; }

    Role[] getRoles() { return roles; }
    void setRoles(Role[] value) { this.roles = value; }
}