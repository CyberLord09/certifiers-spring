package com.open.spring.mvc.grades;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String uid;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Double score;

    @Column(name = "class")
    @com.fasterxml.jackson.annotation.JsonProperty("class")
    private String className;

    @Column(columnDefinition = "TEXT")
    private String content; // Stored as JSON string of filenames

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 512)
    private String link;

    public Grade() {
    }

    public Grade(String uid, String title, Double score, String className) {
        this.uid = uid;
        this.title = title;
        this.score = score;
        this.className = className;
        this.content = "[]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getContent() {
        try {
            if (this.content == null)
                return List.of();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.content, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            return List.of();
        }
    }

    public void setContent(List<String> content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.content = mapper.writeValueAsString(content);
        } catch (IOException e) {
            this.content = "[]";
        }
    }

    // Helper for direct JSON string access if needed
    public String getContentJson() {
        return content;
    }

    public void setContentJson(String contentJson) {
        this.content = contentJson;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
