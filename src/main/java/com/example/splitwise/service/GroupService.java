package com.example.splitwise.service;

import com.example.splitwise.exception.ResourceNotFoundException;
import com.example.splitwise.model.Expense;
import com.example.splitwise.model.Group;
import com.example.splitwise.model.Settlement;
import com.example.splitwise.repository.ExpenseRepository;
import com.example.splitwise.repository.GroupRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The core of "group support": every expense, balance, and settlement is
 * scoped to a single Group rather than one global pool.
 *
 * Balances are NOT stored anywhere - they're computed on demand by
 * replaying a group's expenses (see computeNetBalances). This avoids ever
 * having a stale/inconsistent stored balance sitting next to the expenses
 * that actually determine it; the expenses are the single source of truth.
 */
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final MemberService memberService;
    private final MinCashFlowSolver minCashFlowSolver = new MinCashFlowSolver();

    public GroupService(GroupRepository groupRepository,
            ExpenseRepository expenseRepository,
            MemberService memberService) {
        this.groupRepository = groupRepository;
        this.expenseRepository = expenseRepository;
        this.memberService = memberService;
    }

    /**
     * Creates a group, auto-creating any member names that don't already
     * exist (same forgiving behavior as the rest of the app).
     */
    public Group createGroup(String name, List<String> memberNames) {
        memberNames.forEach(memberService::getOrCreateMember);
        Group group = new Group(name, new ArrayList<>(memberNames));
        return groupRepository.save(group);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupOrThrow(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("No group found with id " + groupId));
    }

    /**
     * Adds new members to an existing group's roster. Creates the
     * underlying Member records if they're brand new.
     */
    public Group addMembersToGroup(String groupId, List<String> names) {
        Group group = getGroupOrThrow(groupId);

        for (String name : names) {
            memberService.getOrCreateMember(name);
            if (!group.getMemberNames().contains(name)) {
                group.getMemberNames().add(name);
            }
        }

        return groupRepository.save(group);
    }

    /**
     * Adds an expense scoped to this group. Both the payer and every
     * participant must already be members of the group - this is the
     * "you can only add expenses you're part of" rule, enforced at the
     * group-membership level now, ready to be tightened further once
     * authentication identifies which member is making the request.
     */
    public Expense addExpense(String groupId, String payer, double amount, List<String> participants, String note) {
        Group group = getGroupOrThrow(groupId);

        validateGroupMember(group, payer);
        for (String participant : participants) {
            validateGroupMember(group, participant);
        }

        Expense expense = new Expense(groupId, payer, amount, participants, note);
        return expenseRepository.save(expense);
    }

    public List<Expense> getGroupExpenses(String groupId) {
        getGroupOrThrow(groupId); // 404s cleanly if the group doesn't exist
        return expenseRepository.findByGroupId(groupId);
    }

    /**
     * Computes each group member's net balance by replaying every expense
     * recorded against this group. Members with zero net balance are still
     * included (at 0.0) so the response always reflects the full roster.
     */
    public Map<String, Double> getGroupBalances(String groupId) {
        Group group = getGroupOrThrow(groupId);
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return computeNetBalances(group, expenses);
    }

    /**
     * Runs the min-cash-flow algorithm over this group's current net
     * balances, producing the minimal settle-up transaction list.
     */
    public List<Settlement> getGroupSettlements(String groupId) {
        Map<String, Double> balances = getGroupBalances(groupId);
        return minCashFlowSolver.solve(balances);
    }

    private void validateGroupMember(Group group, String memberName) {
        if (!group.getMemberNames().contains(memberName)) {
            throw new IllegalArgumentException(
                    "'" + memberName + "' is not a member of group '" + group.getName() + "'. "
                            + "Add them via POST /api/groups/" + group.getId() + "/members first.");
        }
    }

    private Map<String, Double> computeNetBalances(Group group, List<Expense> expenses) {
        Map<String, Double> balances = new LinkedHashMap<>();
        for (String memberName : group.getMemberNames()) {
            balances.put(memberName, 0.0);
        }

        for (Expense expense : expenses) {
            double splitAmount = expense.getAmount() / (expense.getParticipants().size() + 1);

            balances.merge(expense.getPayer(), expense.getAmount() - splitAmount, Double::sum);
            for (String participant : expense.getParticipants()) {
                balances.merge(participant, -splitAmount, Double::sum);
            }
        }

        return balances;
    }
}
