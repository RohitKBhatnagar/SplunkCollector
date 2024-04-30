package org.company.collect

class Role {
    private String role;
    private ADGroup[] adGroups;

    String getRole() { return role; }
    void setRole(String value) { this.role = value; }

    ADGroup[] getAdGroups() { return adGroups; }
    void setAdGroups(ADGroup[] value) { this.adGroups = value; }
}