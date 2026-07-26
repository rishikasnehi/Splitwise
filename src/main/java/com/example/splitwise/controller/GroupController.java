package com.example.splitwise.controller;

import com.example.splitwise.model.*;
import com.example.splitwise.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Every expense/balance/settlement operation now lives under a specific
 * group, via the {groupId} path variable - this is what makes "you can
 * only add expenses you're part of" a meaningful rule (part of THIS
 * group), and what lets the same person have independent balances across
 * different trips/households instead of one global number.
 */
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Creates a new group with an initial member roster.
     * POST /api/groups
     * Body: { "name": "Goa Trip", "memberNames": ["Alice", "Bob", "Carol"] }
     */
    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody GroupRequest request) {
        Group group = groupService.createGroup(request.getName(), request.getMemberNames());
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    /**
     * GET /api/groups/{groupId}
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroupOrThrow(groupId));
    }

    /**
     * Adds more members to an existing group's roster.
     * POST /api/groups/{groupId}/members
     * Body: { "names": ["Dave"] }
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<Group> addMembersToGroup(@PathVariable String groupId,
            @Valid @RequestBody MembersRequest request) {
        Group group = groupService.addMembersToGroup(groupId, request.getNames());
        return ResponseEntity.ok(group);
    }

    /**
     * Records an expense scoped to this group. Payer and all participants
     * must already be members of the group.
     * POST /api/groups/{groupId}/expenses
     */
    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<Expense> addExpense(@PathVariable String groupId,
            @Valid @RequestBody ExpenseRequest request) {
        Expense expense = groupService.addExpense(
                groupId,
                request.getPayer(),
                request.getAmount(),
                request.getParticipants(),
                request.getNote());
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    /**
     * GET /api/groups/{groupId}/expenses
     */
    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<Expense>> getGroupExpenses(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroupExpenses(groupId));
    }

    /**
     * Net balance per group member, computed on demand from that group's
     * expenses.
     * GET /api/groups/{groupId}/balances
     */
    @GetMapping("/{groupId}/balances")
    public ResponseEntity<Map<String, Double>> getGroupBalances(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroupBalances(groupId));
    }

    /**
     * The minimal settle-up plan for this group (min-cash-flow algorithm).
     * GET /api/groups/{groupId}/settlements
     */
    @GetMapping("/{groupId}/settlements")
    public ResponseEntity<List<Settlement>> getGroupSettlements(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroupSettlements(groupId));
    }
}
