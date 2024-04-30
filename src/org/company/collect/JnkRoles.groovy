package org.company.collect

class JnkRoles {
    private String folder
    private boolean isParentFolder
    private JnkGroup jnkGroup

    String getFolder() { return folder }
    void setFolder(String value) { this.folder = value }

    boolean getIsParentFolder() { return isParentFolder }
    void setIsParentFolder(boolean value) { this.isParentFolder = value }

    Group getJnkGroup() { return jnkGroup }
    void setJnkGroup(JnkGroup value) { this.jnkGroup = value }
}