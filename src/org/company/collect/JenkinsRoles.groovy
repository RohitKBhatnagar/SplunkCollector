package org.company.collect

class JenkinsRoles {
    private String folder;
    private boolean isParentFolder;
    private Group[] groups;

    String getFolder() { return folder; }
    void setFolder(String value) { this.folder = value; }

    boolean getIsParentFolder() { return isParentFolder; }
    void setIsParentFolder(boolean value) { this.isParentFolder = value; }

    Group[] getGroups() { return groups; }
    void setGroups(Group[] value) { this.groups = value; }
}
/***
// Class -- Group

class Group {
    private String group;
    private Role[] roles;

    String getGroup() { return group; }
    void setGroup(String value) { this.group = value; }

    Role[] getRoles() { return roles; }
    void setRoles(Role[] value) { this.roles = value; }
}

// Class -- Role

class Role {
    private String role;
    private ADGroup[] adGroups;

    String getRole() { return role; }
    void setRole(String value) { this.role = value; }

    ADGroup[] getAdGroups() { return adGroups; }
    void setAdGroups(ADGroup[] value) { this.adGroups = value; }
}

// Class -- ADGroup

class ADGroup {
    private String adGroup;
    private String isUser;
    private String[] members;
    private long count;

    String getAdGroup() { return adGroup; }
    void setAdGroup(String value) { this.adGroup = value; }

    String getIsUser() { return isUser; }
    void setIsUser(String value) { this.isUser = value; }

    String[] getMembers() { return members; }
    void setMembers(String[] value) { this.members = value; }

    long getCount() { return count; }
    void setCount(long value) { this.count = value; }
}
*********/