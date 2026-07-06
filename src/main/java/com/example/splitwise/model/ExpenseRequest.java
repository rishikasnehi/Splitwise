package com.example.splitwise.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Replaces these four console-input functions combined:
 *
 *   def getPayerName():        payer = input("Payer name: ")
 *   def getPaidAmount():       amount = float(input("Amount paid: "))
 *   def getParticipantsName(): loop of input() calls building a list
 *   def getNote():             note = input('Add note: ')
 *
 * One JSON body now carries everything the old code gathered across
 * four separate prompts, e.g.:
 *   {
 *     "payer": "Alice",
 *     "amount": 120.0,
 *     "participants": ["Bob", "Carol"],
 *     "note": "Dinner"
 *   }
 */
public class ExpenseRequest {

    @NotBlank(message = "payer is required")
    private String payer;

    @Positive(message = "amount must be greater than 0")
    private double amount;

    @NotEmpty(message = "participants must contain at least one name")
    private List<String> participants;

    private String note; // optional, matches "Add note:" being freely skippable in the original

    public ExpenseRequest() {
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
