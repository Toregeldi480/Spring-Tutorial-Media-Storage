package com.media_storage.gateway_service.entity;

public class UserDto {
    private String username;
    private String createdAt;
    private Integer fileCount;

    public UserDto(String username, String createdAt, Integer fileCount) {
        this.username  = username;
        this.createdAt = createdAt;
        this.fileCount = fileCount;
    }

    public String getUsername() {
        return this.username;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public Integer getFileCount() {
        return this.fileCount;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }
}
