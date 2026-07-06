package com.example.splitwise.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Direct port of the Python inner class:
 *
 *   class Expense:
 *       def __init__(self, payer, amount, participants, note):
 *           self.payer = payer
 *           self.amount = amount
 *           self.participants = participants
 *           self.note = note
 *
 * Was: appended to the in-memory `expenses` List inside SplitwiseService.
 * Now: a document in MongoDB's "expenses" collection. It still doubles as
 * the JSON shape returned in API responses (e.g. GET /api/expenses) - only
 * now it also carries a Mongo-generated @Id.
 */
@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;

    private String payer;
    private double amount;
    private List<String> participants;
    private String note;

    public Expense() {
        // no-arg constructor required for JSON deserialization by Jackson
    }

    public Expense(String payer, double amount, List<String> participants, String note) {
        this.payer = payer;
        this.amount = amount;
        this.participants = participants;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
