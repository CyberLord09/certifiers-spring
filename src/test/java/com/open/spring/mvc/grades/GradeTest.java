package com.open.spring.mvc.grades;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GradeTest {

    @Test
    public void testGradeFields() {
        Grade grade = new Grade();
        String uid = "gh_12345";

        // Test uid setter/getter
        grade.setUid(uid);
        assertEquals(uid, grade.getUid(), "getUid should return set uid");

        // Test className field (renamed from course)
        String className = "CSA";
        grade.setClassName(className);
        assertEquals(className, grade.getClassName(), "getClassName should return set className");

        // Test title field (renamed from assignment)
        String title = "Lab 1";
        grade.setTitle(title);
        assertEquals(title, grade.getTitle(), "getTitle should return set title");

        // Test new fields
        String notes = "Good work";
        grade.setNotes(notes);
        assertEquals(notes, grade.getNotes(), "getNotes should return set notes");

        String link = "http://example.com";
        grade.setLink(link);
        assertEquals(link, grade.getLink(), "getLink should return set link");
    }
}
