package com.mbu.dslibrary.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.Module;

public interface ModuleRepository extends MongoRepository<Module, String> {
    List<Module> findAllByOrderBySubjectAscModuleNumberAsc();
}
