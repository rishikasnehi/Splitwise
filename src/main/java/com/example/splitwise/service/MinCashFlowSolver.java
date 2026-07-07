package com.example.splitwise.service;

import com.example.splitwise.model.Settlement;

import java.util.*;

/**
 * Min-Cash-Flow settlement algorithm.
 *
 * Problem: given each member's net balance (positive = owed money,
 * negative = owes money), produce the SMALLEST possible list of
 * transactions that settles everyone to zero.
 *
 * Naive approach (what most Splitwise clones do): replay every expense
 * as its own debtor->creditor transaction. For V members and E expenses
 * this can produce up to E transactions, with lots of redundant
 * back-and-forth between the same two people.
 *
 * This approach: treat the settlement as a graph problem.
 * - Vertices (V): members with a non-zero net balance.
 * - Edges (E): the transactions we're about to construct (not the
 * original expenses) — each edge here settles as much debt as
 * possible in one shot.
 *
 * Greedy strategy:
 * 1. Put every creditor (balance > 0) into a max-heap keyed by balance.
 * 2. Put every debtor (balance < 0) into a max-heap keyed by |balance|
 * (i.e. a min-heap on the raw negative balance).
 * 3. Repeatedly pop the single largest creditor and single largest
 * debtor, settle min(creditor balance, debtor's owed amount)
 * between them, and push whichever side still has a nonzero
 * balance back onto its heap.
 * 4. Stop when one heap empties (the other is guaranteed to be empty
 * too, since balances always sum to ~0).
 *
 * Why this is optimal: every step fully zeroes out at least one vertex
 * (the smaller of the two balances involved), so after at most V-1
 * transactions all V vertices are settled. You cannot do better than
 * V-1 transactions in general, because settling V vertices to zero
 * needs at least V-1 edges in the transaction graph (it has to stay
 * connected in the "who paid whom" sense) — so this greedy approach is
 * actually optimal, not just "pretty good."
 *
 * Complexity: building both heaps is O(V log V); each of the O(V) loop
 * iterations does O(log V) heap work; so total time is O(V log V),
 * versus O(E) or worse for the naive replay-every-expense approach.
 * 
 * Let V be the number of members with non-zero balances.

 * Building the two priority queues: O(V log V)
 * Each settlement iteration performs heap poll/offer operations, each O(log V).
 * At least one member is completely settled in each iteration, so there are at most V − 1 iterations.

 * Overall Time Complexity: O(V log V)

 * Space Complexity: O(V) for the two heaps and the result list.
 */
public class MinCashFlowSolver {

    // Balances smaller than this are treated as settled, to avoid
    // infinite-looping or spurious tiny transactions from floating-point drift.
    private static final double EPSILON = 0.01;

    public List<Settlement> solve(Map<String, Double> netBalances) {
        // Max-heap of creditors, ordered by balance descending (largest owed-to first)
        PriorityQueue<Balance> creditors = new PriorityQueue<>(
                Comparator.comparingDouble((Balance b) -> b.amount).reversed());

        // Max-heap of debtors, ordered by |balance| descending (largest owed first).
        // Debtor balances are stored as their absolute (positive) owed amount here,
        // so this is a regular max-heap too — just fed from negative net balances.
        PriorityQueue<Balance> debtors = new PriorityQueue<>(
                Comparator.comparingDouble((Balance b) -> b.amount).reversed());

        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            double balance = entry.getValue();
            if (balance > EPSILON) {
                creditors.add(new Balance(entry.getKey(), balance));
            } else if (balance < -EPSILON) {
                debtors.add(new Balance(entry.getKey(), -balance)); // store as positive owed amount
            }
            // balances within EPSILON of zero need no settlement at all
        }

        List<Settlement> settlements = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance creditor = creditors.poll(); // largest amount owed TO this person
            Balance debtor = debtors.poll(); // largest amount owed BY this person

            double settledAmount = Math.min(creditor.amount, debtor.amount);
            settlements.add(new Settlement(debtor.name, creditor.name, round2(settledAmount)));

            double remainingCredit = creditor.amount - settledAmount;
            double remainingDebt = debtor.amount - settledAmount;

            if (remainingCredit > EPSILON) {
                creditors.add(new Balance(creditor.name, remainingCredit));
            }
            if (remainingDebt > EPSILON) {
                debtors.add(new Balance(debtor.name, remainingDebt));
            }
        }

        return settlements;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Small private holder so the heaps don't need to juggle raw Map.Entry objects.
     */
    private static class Balance {
        final String name;
        final double amount;

        Balance(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }
    }
}
