package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Role;
import com.tuempresa.gestioninventario.repository.RoleRepository;
// import com.tuempresa.gestioninventario.repository.UserRepository; // Si implementas la validación de rol en uso
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    // @Mock
    // private UserRepository userRepository; // Si implementas la validación de rol en uso

    @InjectMocks
    private RoleService roleService;

    private Role roleAdmin;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleAdmin = new Role(1L, "ROLE_ADMIN");
        roleUser = new Role(2L, "ROLE_USER");
    }

    @Test
    void getAllRoles_shouldReturnListOfRoles() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleAdmin, roleUser));

        List<Role> roles = roleService.getAllRoles();

        assertNotNull(roles);
        assertEquals(2, roles.size());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void getRoleById_whenIdIsValidAndRoleExists_shouldReturnRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));

        Optional<Role> foundRole = roleService.getRoleById(1L);

        assertTrue(foundRole.isPresent());
        assertEquals(roleAdmin.getName(), foundRole.get().getName());
        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    void getRoleById_whenIdIsValidAndRoleDoesNotExist_shouldReturnEmptyOptional() {
        when(roleRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Role> foundRole = roleService.getRoleById(3L);

        assertFalse(foundRole.isPresent());
        verify(roleRepository, times(1)).findById(3L);
    }

    @Test
    void getRoleById_whenIdIsInvalid_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.getRoleById(null));
        assertEquals("El ID del rol debe ser un número positivo.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> roleService.getRoleById(0L));
        assertEquals("El ID del rol debe ser un número positivo.", exception.getMessage());
    }

    @Test
    void createRole_whenDataIsValid_shouldCreateAndReturnRole() {
        Role newRoleDetails = new Role(null, "ROLE_EDITOR");
        when(roleRepository.findByName("ROLE_EDITOR")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(3L); // Simular asignación de ID
            return r;
        });

        Role createdRole = roleService.createRole(newRoleDetails);

        assertNotNull(createdRole);
        assertEquals("ROLE_EDITOR", createdRole.getName());
        assertNotNull(createdRole.getId());
        verify(roleRepository, times(1)).findByName("ROLE_EDITOR");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void createRole_whenNameIsEmpty_shouldThrowException() {
        Role newRole = new Role(null, "");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.createRole(newRole));
        assertEquals("El nombre del rol es obligatorio.", exception.getMessage());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createRole_whenNameIsNull_shouldThrowException() {
        Role newRole = new Role(null, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.createRole(newRole));
        assertEquals("El nombre del rol es obligatorio.", exception.getMessage());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createRole_whenNameAlreadyExists_shouldThrowException() {
        Role newRoleDetails = new Role(null, "ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.createRole(newRoleDetails));
        assertEquals("Ya existe un rol con el nombre: ROLE_ADMIN", exception.getMessage());
        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_whenDataIsValid_shouldUpdateAndReturnRole() {
        Role detailsToUpdate = new Role(null, "ROLE_SUPER_ADMIN");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin)); // roleAdmin es el existente
        when(roleRepository.findByName("ROLE_SUPER_ADMIN")).thenReturn(Optional.empty()); // Nuevo nombre no existe
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Role> updatedRoleOpt = roleService.updateRole(1L, detailsToUpdate);

        assertTrue(updatedRoleOpt.isPresent());
        Role updatedRole = updatedRoleOpt.get();
        assertEquals("ROLE_SUPER_ADMIN", updatedRole.getName());
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByName("ROLE_SUPER_ADMIN");
        verify(roleRepository, times(1)).save(any(Role.class));
    }
    
    @Test
    void updateRole_whenNameIsNotChanged_shouldUpdateAndReturnRole() {
        Role detailsToUpdate = new Role(null, roleAdmin.getName()); // Mismo nombre
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        // findByName no debería ser llamado si el nombre no cambia
        when(roleRepository.save(any(Role.class))).thenReturn(roleAdmin);

        Optional<Role> updatedRoleOpt = roleService.updateRole(1L, detailsToUpdate);

        assertTrue(updatedRoleOpt.isPresent());
        assertEquals(roleAdmin.getName(), updatedRoleOpt.get().getName());
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, never()).findByName(roleAdmin.getName()); // Verifica que no se llame si el nombre es igual
        verify(roleRepository, times(1)).save(roleAdmin);
    }

    @Test
    void updateRole_whenRoleNotFound_shouldReturnEmptyOptional() {
        Role detailsToUpdate = new Role(null, "ROLE_UNKNOWN");
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Role> updatedRoleOpt = roleService.updateRole(99L, detailsToUpdate);

        assertFalse(updatedRoleOpt.isPresent());
        verify(roleRepository, times(1)).findById(99L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_whenNewNameIsEmpty_shouldThrowException() {
        Role detailsToUpdate = new Role(null, "");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.updateRole(1L, detailsToUpdate));
        assertEquals("El nombre del rol es obligatorio.", exception.getMessage());
        verify(roleRepository, times(1)).findById(1L));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_whenNewNameAlreadyExistsForAnotherRole_shouldThrowException() {
        Role detailsToUpdate = new Role(null, "ROLE_USER"); // Nombre de roleUser
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.updateRole(1L, detailsToUpdate));
        assertEquals("Ya existe otro rol con el nombre: ROLE_USER", exception.getMessage());
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void deleteRole_whenRoleExists_shouldReturnTrue() {
        when(roleRepository.existsById(1L)).thenReturn(true);
        // Si se implementa la comprobación de usuarios:
        // when(userRepository.countByRoles_Id(1L)).thenReturn(0L);
        doNothing().when(roleRepository).deleteById(1L);

        boolean deleted = roleService.deleteRole(1L);

        assertTrue(deleted);
        verify(roleRepository, times(1)).existsById(1L);
        // verify(userRepository, times(1)).countByRoles_Id(1L); // Si se implementa
        verify(roleRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRole_whenRoleDoesNotExist_shouldReturnFalse() {
        when(roleRepository.existsById(99L)).thenReturn(false);

        boolean deleted = roleService.deleteRole(99L);

        assertFalse(deleted);
        verify(roleRepository, times(1)).existsById(99L);
        verify(roleRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRole_whenIdIsInvalid_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleService.deleteRole(null));
        assertEquals("El ID del rol a eliminar debe ser un número positivo.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> roleService.deleteRole(0L));
        assertEquals("El ID del rol a eliminar debe ser un número positivo.", exception.getMessage());
    }

    /*
    // Ejemplo de test si se implementa la validación de rol en uso:
    @Test
    void deleteRole_whenRoleIsInUse_shouldThrowIllegalStateException() {
        when(roleRepository.existsById(1L)).thenReturn(true);
        when(userRepository.countByRoles_Id(1L)).thenReturn(5L); // 5 usuarios tienen este rol

        Exception exception = assertThrows(IllegalStateException.class, () -> roleService.deleteRole(1L));
        assertEquals("No se puede eliminar el rol porque está asignado a 5 usuario(s).", exception.getMessage());

        verify(roleRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).countByRoles_Id(1L);
        verify(roleRepository, never()).deleteById(anyLong());
    }
    */
}