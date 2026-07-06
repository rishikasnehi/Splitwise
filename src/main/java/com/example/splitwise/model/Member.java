package com.example.splitwise.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Was: an entry in the in-memory `users` Map<String, Double> inside SplitwiseService.
 * Now: a document in MongoDB's "members" collection.
 *
 * @Document tells Spring Data which collection this maps to.
 * @Id marks the Mongo-generated primary key (an ObjectId, exposed here as a String).
 */
@Document(collection = "members")
public class Member {

    @Id
    private String id;

    private String name;

    private double balance;

    public Member() {
        // required by Spring Data for materializing documents
    }

    public Member(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
