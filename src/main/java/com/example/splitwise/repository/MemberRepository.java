package com.example.splitwise.repository;

import com.example.splitwise.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data generates the implementation of this interface at runtime -
 * you never write a MemberRepositoryImpl class yourself.
 *
 * MongoRepository<Member, String> gives you findAll(), save(), deleteById(),
 * etc. for free. findByName() is a "query derivation" method: Spring Data
 * parses the method name and builds the Mongo query automatically
 * (equivalent to db.members.findOne({ name: ... }) in the Mongo shell).
 */
public interface MemberRepository extends MongoRepository<Member, String> {

    Optional<Member> findByName(String name);

    boolean existsByName(String name);
}
