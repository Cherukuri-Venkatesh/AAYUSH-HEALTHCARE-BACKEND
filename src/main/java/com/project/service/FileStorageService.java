package com.project.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final String PRESCRIPTION_DIR = "uploads/prescriptions/";
    private static final String LABREPORT_DIR = "uploads/labreports/";
    private static final String DOCTOR_DIR = "uploads/doctors/";

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    public String savePrescription(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(PRESCRIPTION_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload prescription");
        }
    }

    public String saveLabReport(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(LABREPORT_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload lab report");
        }
    }

    public String saveDoctorPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Doctor image is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Only JPG, PNG, WEBP image types are allowed");
        }

        try {
            Path uploadPath = Paths.get(DOCTOR_DIR).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "doctor.jpeg" : file.getOriginalFilename()
            );

            String extension = ".jpeg";
            int lastDot = originalName.lastIndexOf('.');
            if (lastDot >= 0) {
                String ext = originalName.substring(lastDot).toLowerCase();
                if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp")) {
                    extension = ext;
                }
            }

            String fileName = "doctor_" + UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(fileName).normalize();

            if (!targetPath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "uploads/doctors/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload doctor image");
        }
    }
}