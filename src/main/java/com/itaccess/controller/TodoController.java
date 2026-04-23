package com.itaccess.controller;

import com.itaccess.dto.TodoDTO;
import com.itaccess.dto.TodoRequest;
import com.itaccess.security.CurrentUser;
import com.itaccess.security.UserInfo;
import com.itaccess.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@Tag(name = "Todos", description = "Endpoints pour la gestion des tâches")
public class TodoController {
    
    private final TodoService todoService;
    
    @GetMapping
    @Operation(summary = "Liste des tâches", description = "Retourne toutes les tâches (admin voit toutes les tâches)")
    public ResponseEntity<List<TodoDTO>> getAllTodos(@CurrentUser UserInfo currentUser) {
        if ("admin".equals(currentUser.getRole())) {
            return ResponseEntity.ok(todoService.getAll());
        }
        return ResponseEntity.ok(todoService.getAllByUser(currentUser.getId()));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Tâche par ID", description = "Retourne une tâche par son ID")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Créer une tâche", description = "Crée une nouvelle tâche")
    public ResponseEntity<TodoDTO> createTodo(
            @Valid @RequestBody TodoRequest request,
            @CurrentUser UserInfo currentUser) {
        TodoDTO created = todoService.create(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une tâche", description = "Modifie une tâche existante")
    public ResponseEntity<TodoDTO> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(todoService.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une tâche", description = "Supprime une tâche")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Basculer l'état", description = "Marque une tâche comme terminée/non terminée")
    public ResponseEntity<TodoDTO> toggleTodo(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.toggleComplete(id));
    }
}
