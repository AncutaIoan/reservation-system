package com.reservationsystem.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(User::new)
                .toList();
    }

    public User createUser(String email, String displayName) {
        UserEntity userEntity = new UserEntity(email, displayName);
        return new User(userRepository.save(userEntity));
    }

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .map(User::new)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
