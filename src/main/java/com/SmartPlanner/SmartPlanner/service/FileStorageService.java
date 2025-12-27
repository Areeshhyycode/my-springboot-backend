package com.SmartPlanner.SmartPlanner.service;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * ENHANCED FILE STORAGE SERVICE
 * File upload/delete with image processing capabilities
 */
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    // Allowed image extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    // Max file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Image dimensions for profile photos
    private static final int PROFILE_IMAGE_SIZE = 400;

    /**
     * Upload file with image optimization
     */
    public String uploadFile(MultipartFile file, String subfolder) throws IOException {
        // Validate file
        validateFile(file);

        // Create directory if not exists
        Path uploadPath = Paths.get(uploadDir, subfolder);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Process and resize image if it's a profile photo
        if (subfolder.contains("profile-photos")) {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = resizeImage(originalImage, PROFILE_IMAGE_SIZE);

            // Save resized image
            ImageIO.write(resizedImage, extension, filePath.toFile());
        } else {
            // Save without processing
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return URL
        return baseUrl + "/" + subfolder + "/" + uniqueFilename;
    }

    /**
     * Delete file from storage
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
                return;
            }

            // Extract relative path from URL
            String relativePath = fileUrl.substring(baseUrl.length() + 1);
            Path filePath = Paths.get(uploadDir, relativePath);

            // Delete file
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + fileUrl);
            e.printStackTrace();
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only these image formats are allowed: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    /**
     * Resize image to specified dimensions (maintains aspect ratio)
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetSize) {
        return Scalr.resize(originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                targetSize,
                targetSize,
                Scalr.OP_ANTIALIAS);
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}