package com.mbu.dslibrary.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.Experiment;

public interface ExperimentRepository extends MongoRepository<Experiment, String> {
}
