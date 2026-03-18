package com.media_storage.user_service.controller;

import com.media_storage.user_service.dto.UserDto;
import com.media_storage.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader(value = "X-Username") String username) {
        return ResponseEntity.ok().body(userService.me(username));
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<UserDto>> all() {
        return ResponseEntity.ok().body(userService.all());
    }

    @PostMapping("/changeFileCount")
    public void changeFileCount(@RequestHeader("X-Username") String username, @RequestParam("amount") int amount) {
        userService.changeFileCount(username, amount);
    }
}
