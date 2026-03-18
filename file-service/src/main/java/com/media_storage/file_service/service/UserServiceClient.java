package com.media_storage.file_service.service;

import com.media_storage.file_service.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", configuration = FeignConfiguration.class)
public interface UserServiceClient {
    @PostMapping("/user/changeFileCount")
    void changeFileCount(@RequestHeader("X-Username") String username, @RequestParam("amount") int amount);
}
