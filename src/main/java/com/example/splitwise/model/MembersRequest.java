package com.example.splitwise.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Replaces this console interaction:
 *
 *   n = int(input('Members Count - '))
 *   for i in range(n):
 *       print('Member-', i+1, ' Name - ')
 *       participant_name = input()
 *
 * The client now just POSTs the full list of names as JSON, e.g.:
 *   { "names": ["Alice", "Bob", "Carol"] }
 *
 * We don't need a separate "count" field — the count is just names.size().
 */
public class MembersRequest {

    @NotEmpty(message = "names must contain at least one member")
    private List<String> names;

    public MembersRequest() {
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}
