package com.example.splitwise.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "groups")
public class Group {

    @Id
    private String id;

    private String name;

    private List<String> memberNames;

    public Group() {
    }

    public Group(String name, List<String> memberNames) {
        this.name = name;
        this.memberNames = memberNames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
