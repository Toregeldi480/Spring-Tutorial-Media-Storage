package com.media_storage.user_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class UserDto {
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date createdAt;
    private Integer fileCount;

    public UserDto(String username, Date createdAt, Integer fileCount) {
        this.username  = username;
        this.createdAt = createdAt;
        this.fileCount = fileCount;
    }

    public String getUsername() {
        return this.username;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public Integer getFileCount() {
        return this.fileCount;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }
}
