package com.itaccess.controller;

import com.itaccess.dto.PasswordChangeRequest;
import com.itaccess.dto.UserDTO;
import com.itaccess.security.CurrentUser;
import com.itaccess.security.UserInfo;
import com.itaccess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints pour la gestion des utilisateurs")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur (admin uniquement)")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        UserDTO created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Liste des utilisateurs", description = "Retourne tous les utilisateurs (admin uniquement)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Utilisateur par ID", description = "Retourne un utilisateur par son ID (admin uniquement)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Modifier un utilisateur", description = "Modifie un utilisateur existant (admin uniquement)")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur (admin uniquement)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/me")
    @Operation(summary = "Profil actuel", description = "Retourne le profil de l'utilisateur connecté")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser UserInfo currentUser) {
        return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
    }
    
    @PutMapping("/me")
    @Operation(summary = "Modifier le profil", description = "Met à jour le profil de l'utilisateur connecté")
    public ResponseEntity<UserDTO> updateCurrentUser(
            @CurrentUser UserInfo currentUser,
            @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.updateUserProfile(currentUser.getId(), dto));
    }
    
    @PutMapping("/me/password")
    @Operation(summary = "Changer le mot de passe", description = "Change le mot de passe de l'utilisateur connecté")
    public ResponseEntity<Void> changePassword(
            @CurrentUser UserInfo currentUser,
            @RequestBody PasswordChangeRequest request) {
        userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}