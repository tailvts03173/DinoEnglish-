package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.CloudWord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudWordRepository extends MongoRepository<CloudWord, String> {
    // Spring Boot tự động cung cấp các hàm save(), findAll(), findById(), delete() cho MongoDB
}