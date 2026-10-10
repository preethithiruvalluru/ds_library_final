package com.mbu.dslibrary.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.Resource;

public interface ResourceRepository extends MongoRepository<Resource, String> {
}
