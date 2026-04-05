package com.project.service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.dto.PatientDTO;
import com.project.dto.PatientProfileUpdateRequest;
import com.project.entity.LabReport;
import com.project.entity.Patient;
import com.project.entity.Prescription;
import com.project.repository.AdminRepository;
import com.project.repository.AppointmentRepository;
import com.project.repository.LabReportRepository;
import com.project.repository.PatientRepository;
import com.project.repository.PrescriptionRepository;
import com.project.security.AuthContextService;

@Service
public class PatientService {

    private static final Logger logger = Logger.getLogger(PatientService.class.getName());

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private LoginUserService loginUserService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private LabReportRepository labReportRepository;

    public PatientDTO registerPatient(Patient patient) {

        // ✅ Normalize email (IMPORTANT)
        String email = patient.getEmail().trim().toLowerCase();
        patient.setEmail(email);

        // ✅ Check duplicates
        if (patientRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email already exists");
        }

        if (adminRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (patientRepository.existsByPhone(patient.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        if (patient.getPassword() == null || patient.getPassword().trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        // 🔐 FORCE ROLE (CRITICAL SECURITY FIX)
        patient.setRole("PATIENT");

        patient.setPassword(patient.getPassword().trim());

        Patient savedPatient = patientRepository.save(patient);
        loginUserService.upsertPatient(savedPatient);

       // ✅ Send email
emailService.sendEmail(
        savedPatient.getEmail(),
        "Registration Successful - AAYUSH HEALTHCARE",
        "Dear " + savedPatient.getName() + ",\n\n" +
        "We are pleased to inform you that your registration with AAYUSH HEALTHCARE has been successfully completed.\n\n" +
        "Your account is now active and you can access our services using your registered details.\n\n" +
        "If you require any assistance, please feel free to contact our support team.\n\n" +
        "Thank you for choosing AAYUSH HEALTHCARE. We look forward to serving you.\n\n" +
        "Warm regards,\nAAYUSH HEALTHCARE Team"
);

        return modelMapper.map(savedPatient, PatientDTO.class);
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(p -> modelMapper.map(p, PatientDTO.class))
                .collect(Collectors.toList());
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return modelMapper.map(patient, PatientDTO.class);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Long patientId = patient.getId();
        String email = patient.getEmail();

        // Step 1: Delete all physical files from disk before deleting DB records
        deletePatientFiles(patientId);

        // Step 2: Remove dependent rows to avoid FK constraint failures during patient delete
        labReportRepository.deleteByPatient_Id(patientId);
        prescriptionRepository.deleteByPatient_Id(patientId);
        appointmentRepository.deleteByPatient_Id(patientId);

        // Step 3: Delete login entry first, then patient record
        // (patient delete trigger also cleans login_users; this order avoids stale-row delete errors)
        loginUserService.deleteByEmail(email);
        patientRepository.deleteById(patientId);
    }

    /**
     * Delete all lab reports and prescriptions files from the filesystem
     * @param patientId The patient ID whose files should be deleted
     */
    private void deletePatientFiles(Long patientId) {
        // Delete lab report files
        List<LabReport> labReports = labReportRepository.findByPatient_Id(patientId);
        for (LabReport report : labReports) {
            if (report.getReportPath() != null && !report.getReportPath().trim().isEmpty()) {
                deleteFile(report.getReportPath());
            }
        }

        // Delete prescription files
        List<Prescription> prescriptions = prescriptionRepository.findByPatient_Id(patientId);
        for (Prescription prescription : prescriptions) {
            if (prescription.getFilePath() != null && !prescription.getFilePath().trim().isEmpty()) {
                deleteFile(prescription.getFilePath());
            }
        }
    }

    /**
     * Safely delete a file from the filesystem
     * @param filePath The full file path to delete
     */
    private void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    logger.info("✓ Deleted file: " + filePath);
                } else {
                    logger.warning("⚠ Failed to delete file: " + filePath + " (permission denied or file locked)");
                }
            } else {
                logger.warning("⚠ File not found or is not a regular file: " + filePath);
            }
        } catch (Exception e) {
            logger.warning("⚠ Error deleting file " + filePath + ": " + e.getMessage());
            // Don't throw exception - continue with DB deletion even if file deletion fails
        }
    }

    public PatientDTO getCurrentPatientProfile() {
        Long patientId = authContextService.getCurrentPatientId();
        return getPatientById(patientId);
    }

    public PatientDTO updateCurrentPatientProfile(PatientProfileUpdateRequest request) {
        if (!authContextService.isPatient()) {
            throw new RuntimeException("Unauthorized");
        }

        Long patientId = authContextService.getCurrentPatientId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        patient.setName(request.getName().trim());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setAddressLine1(request.getAddressLine1());
        patient.setAddressLine2(request.getAddressLine2());

        Patient saved = patientRepository.save(patient);
        loginUserService.upsertPatient(saved);
        return modelMapper.map(saved, PatientDTO.class);
    }

    public void updateCurrentPatientPassword(String newPassword) {
        if (!authContextService.isPatient()) {
            throw new RuntimeException("Unauthorized");
        }

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        Long patientId = authContextService.getCurrentPatientId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setPassword(newPassword.trim());
        Patient saved = patientRepository.save(patient);
        loginUserService.upsertPatient(saved);
    }
}