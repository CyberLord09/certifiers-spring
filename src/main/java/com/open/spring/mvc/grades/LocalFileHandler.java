package com.open.spring.mvc.grades;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Comparator;

@Service
@ConditionalOnProperty(name = "file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileHandler implements FileHandler {

    @Value("${file.upload-dir:volumes/uploads}")
    private String backendUploadDir;

    @Override
    public String uploadFile(String base64Data, String filename, String uid, String assignmentTitle) {
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

    @Override
    public String decodeFile(String uid, String assignmentTitle, String filename) {
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

    @Override
    public boolean deleteFiles(String uid, String assignmentTitle) {
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
}
