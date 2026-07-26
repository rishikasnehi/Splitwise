package com.example.splitwise.repository;

import com.example.splitwise.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {

}
