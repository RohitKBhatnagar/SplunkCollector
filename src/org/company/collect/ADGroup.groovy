package org.company.collect

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
