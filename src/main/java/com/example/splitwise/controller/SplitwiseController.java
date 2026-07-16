package com.example.splitwise.controller;

import com.example.splitwise.model.Expense;
import com.example.splitwise.model.ExpenseRequest;
import com.example.splitwise.model.Member;
import com.example.splitwise.model.MembersRequest;
import com.example.splitwise.model.Settlement;
import com.example.splitwise.service.SplitwiseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * This class replaces the whole console menu:
 *
 * while True:
 * print("1. Add Members")
 * print("2. Add expense")
 * print("3. Show outstanding balance")
 * print("4. Simplify debts")
 * print("5. Exit")
 * choice = input("Choose an option: ")
 * if choice == '1': splitwise.addMembers()
 * elif choice == '2': splitwise.add_expense()
 * elif choice == '3': splitwise.showParticipantsOutstandingBalance()
 * elif choice == '4': splitwise.simplifyDebts()
 * elif choice == '5': break
 *
 * Each numbered menu option becomes its own HTTP endpoint. There's no
 * "option 5 (Exit)" endpoint - a REST API doesn't need one; you just
 * stop calling it. The while-loop itself disappears entirely: the
 * embedded server keeps listening for requests instead of looping on input().
 */
@RestController
@RequestMapping("/api")
public class SplitwiseController {

    private final SplitwiseService splitwiseService;

    // Spring "injects" the singleton SplitwiseService here automatically
    // because SplitwiseService is annotated @Service.
    public SplitwiseController(SplitwiseService splitwiseService) {
        this.splitwiseService = splitwiseService;
    }

    /**
     * Was menu option 1: Add Members.
     * POST /api/members
     * Body: { "names": ["Alice", "Bob", "Carol"] }
     */
    @PostMapping("/members")
    public ResponseEntity<List<Member>> addMembers(@Valid @RequestBody MembersRequest request) {
        List<Member> members = splitwiseService.addMembers(request.getNames());
        return ResponseEntity.status(HttpStatus.CREATED).body(members);
    }

    /**
     * Was menu option 2: Add expense.
     * POST /api/expenses
     * Body: { "payer": "Alice", "amount": 120, "participants": ["Bob","Carol"],
     * "note": "Dinner" }
     */
    @PostMapping("/expenses")
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody ExpenseRequest request) {
        Expense expense = splitwiseService.addExpense(
                request.getPayer(),
                request.getAmount(),
                request.getParticipants(),
                request.getNote());
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    /**
     * Bonus endpoint - the original printExpenses() was never wired into the
     * menu loop, but it existed, so it's included here too.
     * GET /api/expenses
     */
    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getExpenses() {
        return ResponseEntity.ok(splitwiseService.getExpenses());
    }

    /**
     * Was menu option 3: Show outstanding balance.
     * GET /api/balances
     */
    @GetMapping("/balances")
    public ResponseEntity<Map<String, Double>> getBalances() {
        return ResponseEntity.ok(splitwiseService.getOutstandingBalances());
    }

    /**
     * NEW: the min-cash-flow optimization. Instead of reporting every
     * payer/participant pair from expense history (see /api/debts/simplify),
     * this computes the mathematically minimal set of transactions needed
     * to settle the whole group to zero, using a max-heap/max-heap greedy
     * algorithm (see MinCashFlowSolver). At most V-1 transactions for V
     * members with a nonzero balance.
     * GET /api/debts/settle
     */
    @GetMapping("/debts/settle")
    public ResponseEntity<List<Settlement>> settleDebts() {
        return ResponseEntity.ok(splitwiseService.settleDebts());
    }

    /**
     * Was menu option 4: Simplify debts.
     * GET /api/debts/simplify
     */
    @GetMapping("/debts/simplify")
    public ResponseEntity<Map<String, Map<String, Double>>> simplifyDebts() {
        return ResponseEntity.ok(splitwiseService.simplifyDebts());
    }
}