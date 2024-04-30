package org.company.collect

class JnkRole {
    private String roleName
    private JnkADGroup jnkADGroup

    String getRoleName() { return roleName }
    void setRoleName(String value) { this.roleName = value }

    JnkADGroup[] getJnkADGroup() { return jnkADGroup }
    void setJnkADGroup(JnkADGroup value) { this.jnkADGroup = value }
}
