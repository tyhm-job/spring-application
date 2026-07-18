package com.example.redis_demo.controller;

import com.example.redis_demo.dto.TodoRequest;
import com.example.redis_demo.mapper.TodoMapper;
import com.example.redis_demo.model.Todo;
import com.example.redis_demo.repository.TodoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    public TodoController(TodoRepository todoRepository, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
    }

    @GetMapping
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Todo> addTodo(@Valid @RequestBody TodoRequest request) {
        Todo todo = todoMapper.toEntity(request);

        return ResponseEntity.ok(todoRepository.save(todo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getById(@PathVariable Long id) {
        return todoRepository.findById(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Todo> markAsCompleted(@PathVariable Long id) {
        return todoRepository.findById(id)
                                .map(todo -> {
                                    todo.setCompleted(true);
                                    return ResponseEntity.ok(todoRepository.save(todo));
                                })
                                .orElse(ResponseEntity.notFound().build());
    }
}
