package com.example.splitwise.service;

import com.example.splitwise.model.Expense;
import com.example.splitwise.model.Settlement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is the "prove it" test for the min-cash-flow optimization.
 *
 * It builds a moderately complex scenario (members with several overlapping
 * expenses - the kind of thing that happens naturally in a real trip/house
 * group) and compares two ways of settling it up:
 *
 * NAIVE approach: what a basic Splitwise clone does - record one debt
 * line per participant, per expense, with no netting at all. If Alice
 * pays for dinner with Bob and Carol, then Bob pays for a cab with
 * Alice, that's 3 separate debt lines, even though in reality Alice and
 * Bob's shares partially cancel out.
 *
 * OPTIMIZED approach: net every member down to a single balance first,
 * then run MinCashFlowSolver (the max-heap/max-heap greedy algorithm)
 * to produce the mathematically minimal transaction list - at most V-1
 * transactions for V members with a nonzero balance.
 *
 * No Spring context or MongoDB is involved - this is a plain, fast JUnit
 * test operating on in-memory Expense objects, so it can run as part of
 * every `mvn test` without needing a database connection.
 */
class NaiveVsOptimizedSettlementTest {

    private final MinCashFlowSolver solver = new MinCashFlowSolver();

    @Test
    @DisplayName("optimized settlement produces fewer transactions than the naive per-expense approach")
    void optimizedProducesFewerTransactionsThanNaive() {
        List<Expense> expenses = buildOverlappingExpenseScenario();
        Set<String> members = collectMembers(expenses);

        int naiveTransactionCount = countNaiveTransactions(expenses);

        Map<String, Double> netBalances = computeNetBalances(expenses);
        List<Settlement> optimizedSettlements = solver.solve(netBalances);
        int optimizedTransactionCount = optimizedSettlements.size();

        printComparison(members.size(), expenses.size(), naiveTransactionCount, optimizedTransactionCount);

        // The core claim: optimization actually reduces transaction count
        assertTrue(optimizedTransactionCount < naiveTransactionCount,
                "Optimized settlement should require fewer transactions than the naive per-expense replay");

        // The theoretical guarantee from the algorithm: never more than V-1
        // transactions
        assertTrue(optimizedTransactionCount <= members.size() - 1,
                "Optimized settlement should never exceed V-1 transactions for V members");
    }

    /**
     * Builds a 12-member scenario with 24 overlapping expenses - enough
     * overlap that the naive approach accumulates redundant back-and-forth
     * debt lines between the same pairs of people, which is exactly what
     * the optimized approach nets away.
     *
     * Deterministic (fixed seed) so the test is reproducible and the
     * printed comparison numbers don't change between runs.
     */
    private List<Expense> buildOverlappingExpenseScenario() {
        List<String> members = List.of(
                "Alice", "Bob", "Carol", "Dave", "Eve", "Frank",
                "Grace", "Heidi", "Ivan", "Judy", "Mallory", "Niaj");

        Random random = new Random(42); // fixed seed for reproducibility
        List<Expense> expenses = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            String payer = members.get(random.nextInt(members.size()));

            // Each expense splits between 2-4 other members besides the payer,
            // deliberately reusing the same people across expenses to create
            // the kind of overlapping debt a naive system wouldn't net out.
            List<String> pool = new ArrayList<>(members);
            pool.remove(payer);
            Collections.shuffle(pool, random);
            int participantCount = 2 + random.nextInt(3); // 2 to 4 participants
            List<String> participants = pool.subList(0, participantCount);

            double amount = 100 + random.nextInt(400); // 100 - 499
            expenses.add(new Expense(payer, amount, new ArrayList<>(participants), "Expense #" + (i + 1)));
        }

        return expenses;
    }

    /**
     * Naive transaction count: one debt line per (payer, participant) pair,
     * per expense - exactly what you'd get if you recorded every expense's
     * splits as its own set of transactions with no consolidation at all.
     */
    private int countNaiveTransactions(List<Expense> expenses) {
        int count = 0;
        for (Expense expense : expenses) {
            count += expense.getParticipants().size();
        }
        return count;
    }

    /**
     * Pure re-implementation of the same balance math used in
     * SplitwiseService.addExpense(), without touching Mongo - this test
     * only needs the arithmetic, not persistence.
     */
    private Map<String, Double> computeNetBalances(List<Expense> expenses) {
        Map<String, Double> balances = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            balances.putIfAbsent(expense.getPayer(), 0.0);
            for (String participant : expense.getParticipants()) {
                balances.putIfAbsent(participant, 0.0);
            }

            double splitAmount = expense.getAmount() / (expense.getParticipants().size() + 1);

            balances.merge(expense.getPayer(), expense.getAmount() - splitAmount, Double::sum);
            for (String participant : expense.getParticipants()) {
                balances.merge(participant, -splitAmount, Double::sum);
            }
        }

        return balances;
    }

    private Set<String> collectMembers(List<Expense> expenses) {
        Set<String> members = new LinkedHashSet<>();
        for (Expense expense : expenses) {
            members.add(expense.getPayer());
            members.addAll(expense.getParticipants());
        }
        return members;
    }

    private void printComparison(int memberCount, int expenseCount,
            int naiveCount, int optimizedCount) {
        double reductionPercent = 100.0 * (naiveCount - optimizedCount) / naiveCount;

        System.out.println();
        System.out.println("=== Naive vs Optimized Settlement Comparison ===");
        System.out.println("Members:                " + memberCount);
        System.out.println("Expenses recorded:      " + expenseCount);
        System.out.println("Naive transactions:     " + naiveCount + "  (one per payer/participant pair, no netting)");
        System.out.println("Optimized transactions: " + optimizedCount + "  (min-cash-flow, max-heap/max-heap greedy)");
        System.out.printf("Reduction:               %.1f%%%n", reductionPercent);
        System.out.println("V-1 upper bound:         " + (memberCount - 1));
        System.out.println("=================================================");
        System.out.println();
    }
}
