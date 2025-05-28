package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Role;
import com.tuempresa.gestioninventario.entity.User;
import com.tuempresa.gestioninventario.repository.RoleRepository;
import com.tuempresa.gestioninventario.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private Role roleAdmin;
    private Role roleEditor;

    @BeforeEach
    void setUp() {
        roleAdmin = new Role(1L, "ROLE_ADMIN");
        roleEditor = new Role(2L, "ROLE_EDITOR");

        user1 = new User();
        user1.setId(1L);
        user1.setUsername("john.doe");
        user1.setPassword("password123");
        user1.setEmail("john.doe@example.com");
        user1.setEnabled(true);
        user1.setRoles(new HashSet<>(Collections.singletonList(roleAdmin)));

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("jane.smith");
        user2.setPassword("securePassword");
        user2.setEmail("jane.smith@example.com");
        user2.setEnabled(false);
        user2.setRoles(new HashSet<>(Arrays.asList(roleAdmin, roleEditor)));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Optional<User> foundUser = userService.getUserById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals("john.doe", foundUser.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnEmptyOptional() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<User> foundUser = userService.getUserById(3L);
        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(3L);
    }

    @Test
    void getUserById_whenIdIsInvalid_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(null), "El ID del usuario debe ser un número positivo.");
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(0L), "El ID del usuario debe ser un número positivo.");
    }

    @Test
    void createUser_whenDataIsValid_shouldCreateAndReturnUser() {
        User newUserDetails = new User();
        newUserDetails.setUsername("newUser");
        newUserDetails.setPassword("newPassword123");
        newUserDetails.setEmail("new.user@example.com");
        newUserDetails.setEnabled(true);

        Set<Long> roleIds = new HashSet<>(Collections.singletonList(roleAdmin.getId()));

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
        when(roleRepository.findById(roleAdmin.getId())).thenReturn(Optional.of(roleAdmin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L); // Simulate ID assignment
            return u;
        });

        User createdUser = userService.createUser(newUserDetails, roleIds);

        assertNotNull(createdUser);
        assertEquals("newUser", createdUser.getUsername());
        assertTrue(createdUser.getRoles().contains(roleAdmin));
        assertNotNull(createdUser.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void createUser_whenUsernameIsEmpty_shouldThrowException() {
        User newUser = new User();
        newUser.setUsername("");
        newUser.setPassword("password123");
        newUser.setEmail("test@example.com");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser, Collections.emptySet()));
        assertEquals("El nombre de usuario es obligatorio.", exception.getMessage());
    }

    @Test
    void createUser_whenPasswordIsTooShort_shouldThrowException() {
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("123"); // Menos de 6 caracteres
        newUser.setEmail("test@example.com");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser, Collections.emptySet()));
        assertEquals("La contraseña debe tener al menos 6 caracteres.", exception.getMessage());
    }

    @Test
    void createUser_whenEmailIsInvalid_shouldThrowException() {
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("password123");
        newUser.setEmail("invalidEmail");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser, Collections.emptySet()));
        assertEquals("Formato de email inválido.", exception.getMessage());
    }


    @Test
    void createUser_whenUsernameAlreadyExists_shouldThrowException() {
        User newUserDetails = new User();
        newUserDetails.setUsername("john.doe"); // Existing username
        newUserDetails.setPassword("password123");
        newUserDetails.setEmail("unique.email@example.com");

        when(userRepository.existsByUsername("john.doe")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUserDetails, Collections.emptySet());
        });
        assertEquals("El nombre de usuario ya está en uso: john.doe", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowException() {
        User newUserDetails = new User();
        newUserDetails.setUsername("unique.user");
        newUserDetails.setPassword("password123");
        newUserDetails.setEmail("john.doe@example.com"); // Existing email

        when(userRepository.existsByUsername("unique.user")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUserDetails, Collections.emptySet());
        });
        assertEquals("El email ya está en uso: john.doe@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_whenRoleDoesNotExist_shouldThrowException() {
        User newUserDetails = new User();
        newUserDetails.setUsername("newUser");
        newUserDetails.setPassword("password123");
        newUserDetails.setEmail("new.user@example.com");

        Set<Long> roleIds = new HashSet<>(Collections.singletonList(99L)); // Non-existent role ID

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUserDetails, roleIds);
        });
        assertEquals("Rol no encontrado con ID: 99", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenDataIsValid_shouldUpdateAndReturnUser() {
        User userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("john.doe.updated");
        userDetailsToUpdate.setEmail("john.updated@example.com");
        userDetailsToUpdate.setPassword("newStrongPass123"); // new password
        userDetailsToUpdate.setEnabled(false);

        Set<Long> newRoleIds = new HashSet<>(Collections.singletonList(roleEditor.getId()));

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.existsByUsername("john.doe.updated")).thenReturn(false);
        when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(roleRepository.findById(roleEditor.getId())).thenReturn(Optional.of(roleEditor));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> updatedUserOpt = userService.updateUser(user1.getId(), userDetailsToUpdate, newRoleIds);

        assertTrue(updatedUserOpt.isPresent());
        User updatedUser = updatedUserOpt.get();
        assertEquals("john.doe.updated", updatedUser.getUsername());
        assertEquals("john.updated@example.com", updatedUser.getEmail());
        assertEquals("newStrongPass123", updatedUser.getPassword()); // Check new password
        assertFalse(updatedUser.isEnabled());
        assertTrue(updatedUser.getRoles().contains(roleEditor));
        assertEquals(1, updatedUser.getRoles().size());

        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void updateUser_withoutChangingPassword_shouldKeepOldPassword() {
        User userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("john.doe.v2"); // Change username
        userDetailsToUpdate.setEmail(user1.getEmail()); // Keep email
        // No password set in userDetailsToUpdate, so it should remain the old one
        userDetailsToUpdate.setEnabled(true);
        
        String oldPassword = user1.getPassword();

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.existsByUsername("john.doe.v2")).thenReturn(false);
        // No need to mock existsByEmail if email is not changing
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Optional<User> updatedUserOpt = userService.updateUser(user1.getId(), userDetailsToUpdate, user1.getRoles().stream().map(Role::getId).collect(Collectors.toSet()));
        
        assertTrue(updatedUserOpt.isPresent());
        User updatedUser = updatedUserOpt.get();
        assertEquals("john.doe.v2", updatedUser.getUsername());
        assertEquals(oldPassword, updatedUser.getPassword()); // Password should be unchanged
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotFound_shouldReturnEmptyOptional() {
        User userDetails = new User(); // Dummy details
        userDetails.setUsername("any");
        userDetails.setPassword("anyPass");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> updatedUserOpt = userService.updateUser(99L, userDetails, Collections.emptySet());

        assertFalse(updatedUserOpt.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_shouldReturnTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted);
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldReturnFalse() {
        when(userRepository.existsById(99L)).thenReturn(false);
        boolean deleted = userService.deleteUser(99L);
        assertFalse(deleted);
        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_whenIdIsInvalid_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(null));
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(-1L));
    }
}