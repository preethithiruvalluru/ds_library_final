package com.mbu.dslibrary.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mbu.dslibrary.model.Note;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findTop5ByOrderByCreatedAtDesc();
}
