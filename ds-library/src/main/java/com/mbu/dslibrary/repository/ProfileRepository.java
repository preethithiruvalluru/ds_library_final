package com.mbu.dslibrary.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.StudentProfile;

public interface ProfileRepository extends MongoRepository<StudentProfile, String> {
}
