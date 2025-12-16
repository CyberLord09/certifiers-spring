package com.open.spring.mvc.grades;

import java.util.List;

public interface FileHandler {
    /**
     * Uploads a file (base64 encoded) to the storage system.
     * 
     * @param base64Data      Base64 encoded file content
     * @param filename        User provided filename
     * @param uid             User ID
     * @param assignmentTitle Assignment Title
     * @return The saved filename or null if failed
     */
    String uploadFile(String base64Data, String filename, String uid, String assignmentTitle);

    /**
     * Retrieves a file and encodes it to base64.
     * 
     * @param uid             User ID
     * @param assignmentTitle Assignment Title
     * @param filename        Filename to retrieve
     * @return Base64 encoded string or null if failed
     */
    String decodeFile(String uid, String assignmentTitle, String filename);

    /**
     * Deletes all files associated with an assignment.
     * 
     * @param uid             User ID
     * @param assignmentTitle Assignment Title
     * @return true if successful
     */
    boolean deleteFiles(String uid, String assignmentTitle);
}
