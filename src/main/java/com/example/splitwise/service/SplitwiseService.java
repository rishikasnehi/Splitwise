package com.example.splitwise.service;

import com.example.splitwise.model.Expense;
import com.example.splitwise.model.Member;
import com.example.splitwise.repository.ExpenseRepository;
import com.example.splitwise.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Same responsibilities as before, but state now lives in MongoDB via the
 * two repositories instead of in-memory `List<Expense>` / `Map<String, Double>`
 * fields. Data now survives an application restart, and this class no
 * longer needs to BE the single source of truth in memory - Mongo is.
 *
 * Everything is injected through the constructor (constructor injection),
 * which is the recommended way to wire Spring beans together.
 */
@Service
public class SplitwiseService {

    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;

    public SplitwiseService(MemberRepository memberRepository, ExpenseRepository expenseRepository) {
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Was: addMembers() populating the in-memory `users` map with 0.0 balances.
     * Now: upserts a Member document per name. Re-adding an existing name is
     * a no-op on their balance (matches the old behavior of just seeding 0.0
     * once - we don't want re-posting the same member to wipe their balance).
     */
    public List<Member> addMembers(List<String> names) {
        return names.stream()
                .map(this::getOrCreateMember)
                .toList();
    }

    /**
     * Was: add_expense() mutating the in-memory `users` map balances directly.
     * Now: loads/creates the relevant Member documents, saves the Expense
     * document, then persists the updated balances back to Mongo.
     */
    public Expense addExpense(String payer, double amount, List<String> participants, String note) {
        Member payerMember = getOrCreateMember(payer);

        Expense expense = new Expense(payer, amount, participants, note);
        expense = expenseRepository.save(expense);

        // Same math as the original: split_amount = amount / (participants + 1)
        double splitAmount = amount / (participants.size() + 1);

        payerMember.setBalance(payerMember.getBalance() + (amount - splitAmount));
        memberRepository.save(payerMember);

        for (String participantName : participants) {
            Member participant = getOrCreateMember(participantName);
            participant.setBalance(participant.getBalance() - splitAmount);
            memberRepository.save(participant);
        }

        return expense;
    }

    /**
     * Was: printExpenses() looping over the in-memory list.
     * Now: expenseRepository.findAll() pulls every document from Mongo.
     */
    public List<Expense> getExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * Was: showParticipantsOutstandingBalance() printing from the in-memory map.
     * Now: reads every Member document and shapes it into the same
     * name -> balance map the API was already returning, so the response
     * contract for GET /api/balances doesn't change for API consumers.
     */
    public Map<String, Double> getOutstandingBalances() {
        Map<String, Double> balances = new LinkedHashMap<>();
        for (Member member : memberRepository.findAll()) {
            balances.put(member.getName(), member.getBalance());
        }
        return balances;
    }

    /**
     * Was: simplifyDebts() iterating the in-memory `users` map and `expenses` list.
     * Now: same algorithm, just sourced from Mongo reads. Behavior is
     * unchanged (including the same caveat noted before: this reports
     * per-payer/per-participant shares, it doesn't net down to a minimal
     * set of transactions).
     */
    public Map<String, Map<String, Double>> simplifyDebts() {
        Map<String, Map<String, Double>> debts = new LinkedHashMap<>();
        List<Member> members = memberRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        for (Member member : members) {
            String key = member.getName();
            for (Expense expense : expenses) {
                if (key.equals(expense.getPayer())) {
                    double splitAmount = expense.getAmount() / (expense.getParticipants().size() + 1);
                    debts.putIfAbsent(key, new LinkedHashMap<>());
                    for (String participant : expense.getParticipants()) {
                        debts.get(key).put(participant, splitAmount);
                    }
                }
            }
        }
        return debts;
    }

    /**
     * Shared helper: look a member up by name, or create+save a fresh one
     * with a 0.0 balance if they don't exist yet. This is what lets
     * addExpense() reference a payer/participant who was never explicitly
     * POSTed to /api/members first (same forgiving behavior the original
     * putIfAbsent-based in-memory version had).
     */
    private Member getOrCreateMember(String name) {
        return memberRepository.findByName(name)
                .orElseGet(() -> memberRepository.save(new Member(name, 0.0)));
    }
}
