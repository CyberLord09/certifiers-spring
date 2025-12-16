package com.open.spring.mvc.grades;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "file.storage-type", havingValue = "prod")
public class S3FileHandler implements FileHandler {

    @Override
    public String uploadFile(String base64Data, String filename, String uid, String assignmentTitle) {
        System.out.println("S3 Upload Not Implemented yet. File: " + filename);
        return null;
    }

    @Override
    public String decodeFile(String uid, String assignmentTitle, String filename) {
        System.out.println("S3 Download Not Implemented yet. File: " + filename);
        return null;
    }

    @Override
    public boolean deleteFiles(String uid, String assignmentTitle) {
        System.out.println("S3 Delete Not Implemented yet.");
        return false;
    }
}
