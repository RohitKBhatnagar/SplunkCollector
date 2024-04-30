package org.company.collect

class JnkADGroup {
    private String adGroupName
    private String isUser
    private String[] members
    private long count

    String getAdGroupName() { return adGroupName }
    void setAdGroupName(String value) { this.adGroupName = value }

    String getIsUser() { return isUser }
    void setIsUser(String value) { this.isUser = value }

    String[] getMembers() { return members }
    void setMembers(String[] value) { this.members = value }

    long getCount() { return count }
    void setCount(long value) { this.count = value }
}
