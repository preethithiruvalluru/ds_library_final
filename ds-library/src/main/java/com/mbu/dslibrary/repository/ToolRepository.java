package com.mbu.dslibrary.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.Tool;

public interface ToolRepository extends MongoRepository<Tool, String> {
    Optional<Tool> findFirstByName(String name);
}
