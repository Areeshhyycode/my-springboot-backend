package com.SmartPlanner.SmartPlanner.service;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final int PROFILE_IMAGE_SIZE = 400;

    public String uploadFile(MultipartFile file, String subfolder) throws IOException {
        validateFile(file);

        Path uploadPath = Paths.get(uploadDir, subfolder);
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        Path filePath = uploadPath.resolve(uniqueFilename);

        if (subfolder.contains("profile-photos")) {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = resizeImage(originalImage, PROFILE_IMAGE_SIZE);

            ImageIO.write(resizedImage, extension, filePath.toFile());
        } else {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return baseUrl + "/" + subfolder + "/" + uniqueFilename;
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
                return;
            }

            String relativePath = fileUrl.substring(baseUrl.length() + 1);
            Path filePath = Paths.get(uploadDir, relativePath);

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + fileUrl);
            e.printStackTrace();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

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

    private BufferedImage resizeImage(BufferedImage originalImage, int targetSize) {
        return Scalr.resize(originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                targetSize,
                targetSize,
                Scalr.OP_ANTIALIAS);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
