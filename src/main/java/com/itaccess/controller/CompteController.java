package com.itaccess.controller;

import com.itaccess.dto.CompteDTO;
import com.itaccess.dto.CompteRequest;
import com.itaccess.security.CurrentUser;
import com.itaccess.security.UserInfo;
import com.itaccess.service.CompteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comptes")
@RequiredArgsConstructor
@Tag(name = "Comptes", description = "Endpoints pour la gestion des comptes")
public class CompteController {
    
    private final CompteService compteService;
    
    @GetMapping
    @Operation(summary = "Liste des comptes", description = "Retourne tous les comptes")
    public ResponseEntity<List<CompteDTO>> getAllComptes() {
        return ResponseEntity.ok(compteService.getAllComptes());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Compte par ID", description = "Retourne un compte par son ID")
    public ResponseEntity<CompteDTO> getCompteById(@PathVariable Long id) {
        return ResponseEntity.ok(compteService.getCompteById(id));
    }
    
    @PostMapping
    @Operation(summary = "Créer un compte", description = "Crée un nouveau compte")
    public ResponseEntity<CompteDTO> createCompte(
            @Valid @RequestBody CompteRequest request,
            @CurrentUser UserInfo currentUser) {
        CompteDTO created = compteService.createCompte(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un compte", description = "Modifie un compte existant")
    public ResponseEntity<CompteDTO> updateCompte(
            @PathVariable Long id,
            @Valid @RequestBody CompteRequest request,
            @CurrentUser UserInfo currentUser) {
        return ResponseEntity.ok(compteService.updateCompte(id, request, currentUser.getId(), currentUser.getRole()));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un compte", description = "Supprime un compte")
    public ResponseEntity<Void> deleteCompte(
            @PathVariable Long id,
            @CurrentUser UserInfo currentUser) {
        compteService.deleteCompte(id, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.noContent().build();
    }
}