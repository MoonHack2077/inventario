package com.tuempresa.gestioninventario.controller;

// Ya no importamos UserRequest
import com.tuempresa.gestioninventario.entity.User;
import com.tuempresa.gestioninventario.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set; // Necesario para los roleIds

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users - Crear un nuevo usuario
    // Pasamos los roleIds como un parámetro de solicitud
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user,
                                        @RequestParam(required = false) Set<Long> roleIds) {
        try {
            User createdUser = userService.createUser(user, roleIds);
            createdUser.setPassword(null); // No devolver la contraseña
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // GET /api/users - Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setPassword(null)); // No mostrar contraseñas
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // GET /api/users/{id} - Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.getUserById(id);
        return userOptional.map(user -> {
                                user.setPassword(null); // No mostrar contraseña
                                return new ResponseEntity<>(user, HttpStatus.OK);
                            })
                           .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // PUT /api/users/{id} - Actualizar un usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody User userDetails,
                                        @RequestParam(required = false) Set<Long> roleIds) {
        try {
            Optional<User> updatedUserOptional = userService.updateUser(id, userDetails, roleIds);
            return updatedUserOptional.map(user -> {
                                        user.setPassword(null); // No mostrar contraseña
                                        return new ResponseEntity<>(user, HttpStatus.OK);
                                    })
                                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // DELETE /api/users/{id} - Eliminar un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}