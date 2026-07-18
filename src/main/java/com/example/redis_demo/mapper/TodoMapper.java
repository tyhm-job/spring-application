package com.example.redis_demo.mapper;

import com.example.redis_demo.dto.TodoRequest;
import com.example.redis_demo.model.Todo;
import lombok.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "completed", constant = "false")
    Todo toEntity(TodoRequest request);
}
