package com.open.spring.mvc.grades;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeRepository gradeRepository;

    @Value("${file.upload-dir:volumes/uploads}")
    private String backendUploadDir;

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No input data provided"));
        }

        String uid = (String) data.get("uid");
        String title = (String) data.get("title");
        String className = (String) data.get("class");
        List<Map<String, String>> uploads = (List<Map<String, String>>) data.get("uploads");
        String notes = (String) data.get("notes");
        String link = (String) data.get("link");

        if (uid == null || title == null || className == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "UID, Title, and Class are required"));
        }

        List<String> uploadedFiles = new ArrayList<>();

        try {
            if (uploads != null) {
                for (Map<String, String> upload : uploads) {
                    String b64File = upload.get("file");
                    String filename = upload.get("filename");

                    if (b64File != null && filename != null) {
                        String savedFilename = assignmentBase64Upload(b64File, filename, uid, title);
                        if (savedFilename != null) {
                            uploadedFiles.add(savedFilename);
                        } else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("message", "Failed to upload file: " + filename));
                        }
                    }
                }
            }

            Grade grade = new Grade(uid, title, 0.0, className);
            grade.setNotes(notes);
            grade.setLink(link);
            grade.setContent(uploadedFiles);

            Grade savedGrade = gradeRepository.save(grade);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertGradeToMap(savedGrade));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateAssignment(@RequestBody Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No input data provided"));
        }

        String uid = (String) data.get("uid");
        String title = (String) data.get("title");
        Object scoreObj = data.get("score");
        String notes = (String) data.get("notes");
        String link = (String) data.get("link");

        if (uid == null || title == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "UID and Title are required to identify assignment"));
        }

        if (scoreObj == null && notes == null && link == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Score, notes, or link is required for update"));
        }

        Optional<Grade> optionalGrade = gradeRepository.findByUidAndTitle(uid, title);
        if (optionalGrade.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Assignment not found"));
        }

        Grade grade = optionalGrade.get();
        if (scoreObj != null) {
            if (scoreObj instanceof Number) {
                grade.setScore(((Number) scoreObj).doubleValue());
            } else if (scoreObj instanceof String) {
                try {
                    grade.setScore(Double.parseDouble((String) scoreObj));
                } catch (NumberFormatException e) {
                    // ignore or error
                }
            }
        }
        if (notes != null)
            grade.setNotes(notes);
        if (link != null)
            grade.setLink(link);

        Grade updatedGrade = gradeRepository.save(grade);
        return ResponseEntity.ok(convertGradeToMap(updatedGrade));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAssignments() {
        List<Grade> grades = gradeRepository.findAll();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Grade grade : grades) {
            Map<String, Object> gradeData = convertGradeToMap(grade);
            List<String> content = grade.getContent();
            List<Map<String, String>> enhancedContent = new ArrayList<>();

            for (String filename : content) {
                String base64Str = assignmentBase64Decode(grade.getUid(), grade.getTitle(), filename);
                Map<String, String> fileMap = new HashMap<>();
                fileMap.put("filename", filename);
                if (base64Str != null) {
                    fileMap.put("file", base64Str);
                } else {
                    fileMap.put("error", "File not found or unreadable");
                }
                enhancedContent.add(fileMap);
            }
            gradeData.put("content", enhancedContent);
            results.add(gradeData);
        }

        return ResponseEntity.ok(results);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> deleteAssignment(@RequestParam String uid, @RequestParam String title) {
        if (uid == null || title == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "UID and Title required"));
        }

        Optional<Grade> optionalGrade = gradeRepository.findByUidAndTitle(uid, title);
        if (optionalGrade.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Assignment not found"));
        }

        // Delete files
        boolean filesDeleted = assignmentDeleteFiles(uid, title);

        // Delete record
        gradeRepository.delete(optionalGrade.get());

        return ResponseEntity.ok(Map.of("message", "Assignment deleted successfully"));
    }

    // Helper Methods

    private String assignmentBase64Upload(String base64Data, String filename, String uid, String assignmentTitle) {
        try {
            byte[] fileData = Base64.getDecoder().decode(base64Data);

            // Clean filename (simple version)
            String cleanFilename = Paths.get(filename).getFileName().toString();

            Path targetDir = Paths.get(backendUploadDir, uid, assignmentTitle);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path filePath = targetDir.resolve(cleanFilename);
            Files.write(filePath, fileData);

            return cleanFilename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String assignmentBase64Decode(String uid, String assignmentTitle, String filename) {
        Path filePath = Paths.get(backendUploadDir, uid, assignmentTitle, filename);
        try {
            if (Files.exists(filePath)) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                return Base64.getEncoder().encodeToString(fileBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean assignmentDeleteFiles(String uid, String assignmentTitle) {
        Path targetDir = Paths.get(backendUploadDir, uid, assignmentTitle);
        try {
            if (Files.exists(targetDir)) {
                // Recursive delete
                Files.walk(targetDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, Object> convertGradeToMap(Grade grade) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", grade.getId());
        map.put("uid", grade.getUid());
        map.put("title", grade.getTitle());
        map.put("class", grade.getClassName());
        map.put("score", grade.getScore());
        map.put("content", grade.getContent()); // Default is List<String>
        map.put("notes", grade.getNotes());
        map.put("link", grade.getLink());
        return map;
    }
}
