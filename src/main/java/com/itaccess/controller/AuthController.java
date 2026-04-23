package com.itaccess.controller;

import com.itaccess.dto.*;
import com.itaccess.entity.User;
import com.itaccess.repository.UserRepository;
import com.itaccess.security.JwtTokenProvider;
import com.itaccess.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints pour l'authentification")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final SettingService settingService;
    
    @PostMapping(value = "/token", consumes = {"application/json", "application/x-www-form-urlencoded"})
    @Operation(summary = "Connexion", description = "Authentifie l'utilisateur et retourne un token JWT")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody(required = false) LoginRequest requestBody,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password) {
        
        String user = username;
        String pass = password;
        
        if (requestBody != null) {
            user = requestBody.getUsername();
            pass = requestBody.getPassword();
        }
        
        if (user == null || pass == null || user.isBlank() || pass.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(TokenResponse.builder()
                            .accessToken("Nom d'utilisateur et mot de passe requis")
                            .tokenType("bearer")
                            .build());
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user, pass)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User existingUser = userRepository.findByUsername(user)
                    .orElse(null);
            
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(TokenResponse.builder()
                                .accessToken("Utilisateur non trouvé")
                                .tokenType("bearer")
                                .build());
            }
            
            if (existingUser.getIsActive() != null && !existingUser.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(TokenResponse.builder()
                                .accessToken("Compte désactivé. Veuillez contacter l'administrateur.")
                                .tokenType("bearer")
                                .build());
            }
            
            String token = jwtTokenProvider.generateToken(authentication);
            
            return ResponseEntity.ok(TokenResponse.builder()
                    .accessToken(token)
                    .tokenType("bearer")
                    .build());
            
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenResponse.builder()
                            .accessToken("Nom d'utilisateur ou mot de passe incorrect")
                            .tokenType("bearer")
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenResponse.builder()
                            .accessToken("Erreur: " + e.getMessage())
                            .tokenType("bearer")
                            .build());
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Utilisateur actuel", description = "Retourne les informations de l'utilisateur actuellement connecté")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        return ResponseEntity.ok(CurrentUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build());
    }
    
    @PostMapping("/refresh-secret-key")
    public ResponseEntity<String> refreshSecretKey() {
        settingService.refreshSecretKey();
        return ResponseEntity.ok("SECRET_KEY vérifié/régénéré avec succès");
    }
}