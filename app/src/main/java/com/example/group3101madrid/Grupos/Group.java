package com.example.group3101madrid.Grupos;

import java.util.HashMap;
import java.util.Map;

// Group.java
public class Group {
    private String groupId;
    private String name;
    private String description;
    private String leaderId;
    private String joinCode;
    private Map<String, Boolean> members; // userId -> true

    public Group() {
        // Required empty constructor for Firebase
    }

    public Group(String name, String description, String leaderId, String joinCode) {
        this.name = name;
        this.description = description;
        this.leaderId = leaderId;
        this.joinCode = joinCode;
        this.members = new HashMap<>();
        this.members.put(leaderId, true); // Add leader as first member
    }

    // Getters and setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }
    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }
    public Map<String, Boolean> getMembers() { return members; }
    public void setMembers(Map<String, Boolean> members) { this.members = members; }
}