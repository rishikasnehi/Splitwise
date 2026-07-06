package com.example.splitwise.repository;

import com.example.splitwise.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Same pattern as MemberRepository. findByPayer() isn't used by the
 * service yet, but it's a natural query to have on hand (e.g. for a
 * future "expenses this person paid for" endpoint) and shows how easy
 * query derivation is to extend.
 */
public interface ExpenseRepository extends MongoRepository<Expense, String> {

    List<Expense> findByPayer(String payer);
}
