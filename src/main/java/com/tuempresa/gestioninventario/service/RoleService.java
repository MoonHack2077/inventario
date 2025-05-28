package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Role;
import com.tuempresa.gestioninventario.repository.RoleRepository;
// Importar UserRepository si vas a validar si un rol está en uso
// import com.tuempresa.gestioninventario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Para validar cadenas

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    // Descomenta si añades la validación de roles en uso
    // private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) { // Añade UserRepository aquí si es necesario
        this.roleRepository = roleRepository;
        // this.userRepository = userRepository;
    }

    private void validateRoleData(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("El objeto rol no puede ser nulo.");
        }
        if (!StringUtils.hasText(role.getName())) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio.");
        }
        // Opcional: Validar formato del nombre del rol, ej. que empiece con "ROLE_"
        // if (!role.getName().startsWith("ROLE_")) {
        //     throw new IllegalArgumentException("El nombre del rol debe comenzar con 'ROLE_'.");
        // }
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del rol debe ser un número positivo.");
        }
        return roleRepository.findById(id);
    }

    @Transactional
    public Role createRole(Role role) {
        validateRoleData(role);
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + role.getName());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public Optional<Role> updateRole(Long id, Role roleDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del rol a actualizar debe ser un número positivo.");
        }
        validateRoleData(roleDetails);

        return roleRepository.findById(id)
            .map(existingRole -> {
                // Validar nombre único si cambia
                if (!existingRole.getName().equalsIgnoreCase(roleDetails.getName()) && // Usar equalsIgnoreCase por si acaso
                    roleRepository.findByName(roleDetails.getName()).isPresent()) {
                    throw new IllegalArgumentException("Ya existe otro rol con el nombre: " + roleDetails.getName());
                }
                existingRole.setName(roleDetails.getName());
                return roleRepository.save(existingRole);
            });
    }

    @Transactional
    public boolean deleteRole(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del rol a eliminar debe ser un número positivo.");
        }
        if (!roleRepository.existsById(id)) {
            // Podrías lanzar una excepción de "No Encontrado" aquí si prefieres,
            // pero para un método que devuelve boolean, retornar false es común.
            return false;
        }

        // Opcional: Validar si el rol está siendo usado por algún usuario.
        // Esto requeriría inyectar UserRepository.
        // long userCountWithRole = userRepository.countByRoles_Id(id); // Necesitarías este método en UserRepository
        // if (userCountWithRole > 0) {
        //     throw new IllegalStateException("No se puede eliminar el rol porque está asignado a " + userCountWithRole + " usuario(s).");
        // }

        roleRepository.deleteById(id);
        return true;
    }
}