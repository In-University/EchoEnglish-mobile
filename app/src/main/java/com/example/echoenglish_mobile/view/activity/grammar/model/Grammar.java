package com.example.echoenglish_mobile.view.activity.grammar.model;



import java.util.List;
import java.io.Serializable; // Add this
public class Grammar implements Serializable {
    private int id;
    private String name;
    private List<Subsection> subsections;
    private List<Content> contents;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Subsection> getSubsections() { return subsections; }
    public void setSubsections(List<Subsection> subsections) { this.subsections = subsections; }
    public List<Content> getContents() { return contents; }
    public void setContents(List<Content> contents) { this.contents = contents; }
}