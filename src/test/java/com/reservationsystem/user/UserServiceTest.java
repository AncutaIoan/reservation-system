package com.reservationsystem.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_usersExist_returnsAllUsers() {
        UserEntity user = new UserEntity("jane@example.com", "Jane Doe");

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertThat(result)
                .hasSize(1)
                .extracting(User::email)
                .containsExactly("jane@example.com");
    }

    @Test
    void createUser_validEmailAndName_savesAndReturnsUser() {
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser("jane@example.com", "Jane Doe");

        assertThat(result.email()).isEqualTo("jane@example.com");
        assertThat(result.displayName()).isEqualTo("Jane Doe");
    }

    @Test
    void getUser_existingId_returnsUser() {
        UserEntity user = new UserEntity("jane@example.com", "Jane Doe");
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUser(userId);

        assertThat(result.email()).isEqualTo("jane@example.com");
    }

    @Test
    void getUser_missingId_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(userId.toString());
    }
}
