package com.mbu.dslibrary.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.SavedCode;

public interface SavedCodeRepository extends MongoRepository<SavedCode, String> {
    List<SavedCode> findAllByOrderByUpdatedAtDesc();
}
