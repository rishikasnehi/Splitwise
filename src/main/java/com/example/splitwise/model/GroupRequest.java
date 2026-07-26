package com.example.splitwise.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * POST /api/groups body, e.g.:
 * { "name": "Goa Trip", "memberNames": ["Alice", "Bob", "Carol"] }
 *
 * Any name that doesn't already exist as a Member is created automatically -
 * same "forgiving" behavior the original in-memory version had.
 */
public class GroupRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotEmpty(message = "memberNames must contain at least one member")
    private List<String> memberNames;

    public GroupRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames;
    }
}
