package com.example.echoenglish_mobile.view.activity.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
     private Long id;
     private String name;
     public Long getId() { return id; }
     public String getName() { return name; }
 }
