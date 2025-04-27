package com.example.echoenglish_mobile.view.activity.grammar.model;



import java.io.Serializable;
import java.util.List;

public class Topic implements Serializable {
    private int id;
    private String name;
    private List<Content> contents;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Content> getContents() { return contents; }
    public void setContents(List<Content> contents) { this.contents = contents; }
}