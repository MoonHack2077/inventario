package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Role;
import com.tuempresa.gestioninventario.entity.User;
import com.tuempresa.gestioninventario.repository.RoleRepository;
import com.tuempresa.gestioninventario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Para validar cadenas

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern; // Para validación de email
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Patrón simple para validación de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    private void validateUserData(User user, boolean isCreateOperation) {
        if (user == null) {
            throw new IllegalArgumentException("El objeto usuario no puede ser nulo.");
        }
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres.");
        }

        if (StringUtils.hasText(user.getEmail()) && !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Formato de email inválido.");
        }

        if (isCreateOperation) {
            if (!StringUtils.hasText(user.getPassword())) {
                throw new IllegalArgumentException("La contraseña es obligatoria al crear un usuario.");
            }
            // Podrías añadir validación de fortaleza de contraseña aquí si es necesario
            if (user.getPassword().length() < 6) {
                 throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
            }
        } else { // En actualización, si se provee contraseña, también validar longitud
            if (StringUtils.hasText(user.getPassword()) && user.getPassword().length() < 6) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
            }
        }
    }


    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser un número positivo.");
        }
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user, Set<Long> roleIds) {
        validateUserData(user, true); // Validar datos del usuario para creación

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso: " + user.getUsername());
        }
        if (StringUtils.hasText(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso: " + user.getEmail());
        }

        // La contraseña ya viene en el objeto 'user' y se validó en validateUserData

        Set<Role> assignedRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            assignedRoles = roleIds.stream()
                    .map(roleId -> {
                        if (roleId == null || roleId <= 0) {
                            throw new IllegalArgumentException("El ID del rol proporcionado es inválido.");
                        }
                        return roleRepository.findById(roleId)
                            .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + roleId));
                    })
                    .collect(Collectors.toSet());
        }
        user.setRoles(assignedRoles);
        // user.setEnabled(true); // 'enabled' debería venir en el objeto user o establecerse aquí si es un valor por defecto.

        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> updateUser(Long id, User userDetails, Set<Long> roleIds) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario a actualizar debe ser un número positivo.");
        }
        validateUserData(userDetails, false); // Validar datos del usuario para actualización

        return userRepository.findById(id)
            .map(existingUser -> {
                // Validar cambio de username
                if (!existingUser.getUsername().equalsIgnoreCase(userDetails.getUsername()) &&
                    userRepository.existsByUsername(userDetails.getUsername())) {
                    throw new IllegalArgumentException("El nuevo nombre de usuario ya está en uso: " + userDetails.getUsername());
                }
                existingUser.setUsername(userDetails.getUsername());

                // Validar cambio de email
                if (StringUtils.hasText(userDetails.getEmail())) {
                    if (existingUser.getEmail() == null || !existingUser.getEmail().equalsIgnoreCase(userDetails.getEmail())) {
                        if (userRepository.existsByEmail(userDetails.getEmail())) {
                             throw new IllegalArgumentException("El nuevo email ya está en uso: " + userDetails.getEmail());
                        }
                     }
                    existingUser.setEmail(userDetails.getEmail());
                } else {
                    existingUser.setEmail(null); // Permitir borrar el email si se envía nulo o vacío
                }

                // Si se proporciona una nueva contraseña, actualizarla
                if (StringUtils.hasText(userDetails.getPassword())) {
                    existingUser.setPassword(userDetails.getPassword());
                }
                // Si no se envía contraseña en userDetails.getPassword(), se mantiene la actual.

                Set<Role> assignedRoles = new HashSet<>();
                if (roleIds != null) { // Permitir un Set vacío para quitar todos los roles
                    assignedRoles = roleIds.stream()
                            .map(roleId -> {
                                if (roleId == null || roleId <= 0) {
                                    throw new IllegalArgumentException("El ID del rol proporcionado es inválido.");
                                }
                                return roleRepository.findById(roleId)
                                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + roleId));
                            })
                            .collect(Collectors.toSet());
                    existingUser.setRoles(assignedRoles);
                }
                // Si roleIds es null, no se modifican los roles existentes.

                existingUser.setEnabled(userDetails.isEnabled());
                return userRepository.save(existingUser);
            });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario a eliminar debe ser un número positivo.");
        }
        if (!userRepository.existsById(id)) {
            return false; // O lanzar UserNotFoundException
        }
        userRepository.deleteById(id);
        return true;
    }
}