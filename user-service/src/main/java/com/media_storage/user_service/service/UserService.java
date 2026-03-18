package com.media_storage.user_service.service;

import com.media_storage.user_service.dto.UserDto;
import com.media_storage.user_service.entity.User;
import com.media_storage.user_service.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto me(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        UserDto dto = new UserDto(user.getUsername(), user.getCreatedAt(), user.getFileCount());
        return dto;
    }

    public Iterable<UserDto> all() {
        Iterable<User> allUsers = userRepository.findAll();
        ArrayList<UserDto> allUserDtos = new ArrayList<>();
        allUsers.forEach(user -> allUserDtos.addLast(new UserDto(user.getUsername(), user.getCreatedAt(), user.getFileCount())));
        return allUserDtos;
    }

    public void changeFileCount(String username, int amount) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setFileCount(user.getFileCount() + amount);
        userRepository.save(user);
    }
}
