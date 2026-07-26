package com.example.splitwise.service;

import com.example.splitwise.model.Member;
import com.example.splitwise.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles member identity only - creating/looking up a Member by name.
 * Balances live entirely in GroupService now, scoped per group.
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> addMembers(List<String> names) {
        return names.stream()
                .map(this::getOrCreateMember)
                .toList();
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Look a member up by name, or create+save a fresh one if they don't
     * exist yet. Shared with GroupService so referencing a brand-new name
     * in a group (or an expense) "just works," matching the original
     * script's forgiving putIfAbsent-style behavior.
     */
    public Member getOrCreateMember(String name) {
        return memberRepository.findByName(name)
                .orElseGet(() -> memberRepository.save(new Member(name)));
    }
}
