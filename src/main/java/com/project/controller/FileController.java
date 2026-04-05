package com.project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.project.entity.LabReport;
import com.project.entity.Prescription;
import com.project.repository.LabReportRepository;
import com.project.repository.PrescriptionRepository;
import com.project.security.AuthContextService;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private LabReportRepository labReportRepository;

    @Autowired
    private AuthContextService authContextService;

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String path) throws IOException {

        String normalizedPath = normalizeDownloadPath(path);

        if (normalizedPath == null) {
            throw new RuntimeException("Unauthorized");
        }

        authorizeFileAccess(path, normalizedPath);

        File file = resolveDownloadFile(normalizedPath);

        if (!file.exists()) {
            throw new RuntimeException("File not found");
        }

        FileInputStream fis = new FileInputStream(file);
        byte[] data = fis.readAllBytes();
        fis.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    private String normalizeDownloadPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return null;
        }

        String normalizedPath = rawPath.trim().replace('\\', '/');

        if (normalizedPath.contains("..")) {
            return null;
        }

        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        int uploadsIndex = normalizedPath.indexOf("uploads/");
        if (uploadsIndex >= 0) {
            normalizedPath = normalizedPath.substring(uploadsIndex);
        }

        if (!(normalizedPath.startsWith("uploads/prescriptions/") ||
                normalizedPath.startsWith("uploads/labreports/"))) {
            return null;
        }

        return normalizedPath;
    }

    private void authorizeFileAccess(String originalPath, String normalizedPath) {

        if (authContextService.isAdmin()) {
            return;
        }

        Optional<Prescription> prescription = resolvePrescription(originalPath, normalizedPath);
        if (prescription.isPresent()) {
            authorizePrescription(prescription.get());
            return;
        }

        Optional<LabReport> labReport = resolveLabReport(originalPath, normalizedPath);
        if (labReport.isPresent()) {
            authorizeLabReport(labReport.get());
            return;
        }

        throw new RuntimeException("Unauthorized");
    }

    private Optional<Prescription> resolvePrescription(String originalPath, String normalizedPath) {
        Optional<Prescription> exact = prescriptionRepository.findByFilePath(normalizedPath);
        if (exact.isPresent()) {
            return exact;
        }

        String originalNormalized = originalPath == null ? "" : originalPath.replace('\\', '/');
        if (!originalNormalized.equals(normalizedPath)) {
            exact = prescriptionRepository.findByFilePath(originalNormalized);
            if (exact.isPresent()) {
                return exact;
            }
        }

        String fileName = extractFileName(normalizedPath);
        if (fileName == null) {
            return Optional.empty();
        }

        Optional<Prescription> slashMatch = prescriptionRepository.findByFilePathEndingWith("/" + fileName);
        if (slashMatch.isPresent()) {
            return slashMatch;
        }

        return prescriptionRepository.findByFilePathEndingWith("\\" + fileName);
    }

    private Optional<LabReport> resolveLabReport(String originalPath, String normalizedPath) {
        Optional<LabReport> exact = labReportRepository.findByReportPath(normalizedPath);
        if (exact.isPresent()) {
            return exact;
        }

        String originalNormalized = originalPath == null ? "" : originalPath.replace('\\', '/');
        if (!originalNormalized.equals(normalizedPath)) {
            exact = labReportRepository.findByReportPath(originalNormalized);
            if (exact.isPresent()) {
                return exact;
            }
        }

        String fileName = extractFileName(normalizedPath);
        if (fileName == null) {
            return Optional.empty();
        }

        Optional<LabReport> slashMatch = labReportRepository.findByReportPathEndingWith("/" + fileName);
        if (slashMatch.isPresent()) {
            return slashMatch;
        }

        return labReportRepository.findByReportPathEndingWith("\\" + fileName);
    }

    private String extractFileName(String normalizedPath) {
        try {
            Path path = Paths.get(normalizedPath);
            Path fileName = path.getFileName();
            return fileName == null ? null : fileName.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private File resolveDownloadFile(String normalizedPath) {
        Path directPath = Paths.get(normalizedPath).normalize();
        if (Files.exists(directPath)) {
            return directPath.toFile();
        }

        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path fromUserDir = userDir.resolve(normalizedPath).normalize();
        if (Files.exists(fromUserDir)) {
            return fromUserDir.toFile();
        }

        Path parentDir = userDir.getParent();
        if (parentDir != null) {
            Path fromParentDir = parentDir.resolve(normalizedPath).normalize();
            if (Files.exists(fromParentDir)) {
                return fromParentDir.toFile();
            }
        }

        return directPath.toFile();
    }

    private void authorizePrescription(Prescription prescription) {
        if (authContextService.isDoctor()) {
            Long doctorId = authContextService.getCurrentDoctorId();
            if (!prescription.getDoctor().getId().equals(doctorId)) {
                throw new RuntimeException("Unauthorized");
            }
            return;
        }

        if (authContextService.isPatient()) {
            Long patientId = authContextService.getCurrentPatientId();
            if (!prescription.getPatient().getId().equals(patientId)) {
                throw new RuntimeException("Unauthorized");
            }
            return;
        }

        throw new RuntimeException("Unauthorized");
    }

    private void authorizeLabReport(LabReport report) {
        if (authContextService.isDoctor()) {
            Long doctorId = authContextService.getCurrentDoctorId();
            if (!report.getDoctor().getId().equals(doctorId)) {
                throw new RuntimeException("Unauthorized");
            }
            return;
        }

        if (authContextService.isPatient()) {
            Long patientId = authContextService.getCurrentPatientId();
            if (!report.getPatient().getId().equals(patientId)) {
                throw new RuntimeException("Unauthorized");
            }
            return;
        }

        throw new RuntimeException("Unauthorized");
    }
}