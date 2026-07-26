package com.example.splitwise.controller;

import com.example.splitwise.model.Member;
import com.example.splitwise.model.MembersRequest;
import com.example.splitwise.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Registers one or more members independent of any group - useful for
     * pre-populating a member directory before assigning people to groups.
     * POST /api/members
     */
    @PostMapping
    public ResponseEntity<List<Member>> addMembers(@Valid @RequestBody MembersRequest request) {
        List<Member> members = memberService.addMembers(request.getNames());
        return ResponseEntity.status(HttpStatus.CREATED).body(members);
    }

    /**
     * GET /api/members
     */
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }
}
