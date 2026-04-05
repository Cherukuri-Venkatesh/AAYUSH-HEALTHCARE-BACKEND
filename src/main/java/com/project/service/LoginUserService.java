package com.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.project.entity.Admindoctor;
import com.project.entity.LoginUser;
import com.project.entity.LoginUserId;
import com.project.entity.Patient;
import com.project.repository.AdminRepository;
import com.project.repository.LoginUserRepository;
import com.project.repository.PatientRepository;

@Service
public class LoginUserService {

    private static final Logger logger = LoggerFactory.getLogger(LoginUserService.class);

    @Autowired
    private LoginUserRepository loginUserRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PatientRepository patientRepository;

    public void upsert(Long id, String name, String email, String password, String role) {
        if (id == null) {
            throw new RuntimeException("ID is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();

        LoginUserId loginUserId = new LoginUserId(id, normalizedRole);

        // Prevent reusing an email already mapped to another account/role.
        loginUserRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            boolean sameRecord = existing.getId() != null
                && existing.getId().equals(id)
                && existing.getRole() != null
                && existing.getRole().equalsIgnoreCase(normalizedRole);

            if (!sameRecord) {
            throw new RuntimeException("Email already exists for another user role");
            }
        });

        LoginUser loginUser = loginUserRepository.findById(loginUserId)
            .orElseGet(LoginUser::new);

        loginUser.setId(id);
        loginUser.setName(name);
        loginUser.setEmail(normalizedEmail);
        loginUser.setPassword(password.trim());
        loginUser.setRole(normalizedRole);

        loginUserRepository.save(loginUser);
    }

    public void upsertDoctor(Admindoctor doctor) {
        if (doctor == null) {
            return;
        }

        upsert(doctor.getId(), doctor.getName(), doctor.getEmail(), doctor.getPassword(), doctor.getRole());
    }

    public void upsertPatient(Patient patient) {
        if (patient == null) {
            return;
        }

        upsert(patient.getId(), patient.getName(), patient.getEmail(), patient.getPassword(), "PATIENT");
    }

    public void deleteByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        loginUserRepository.findByEmailIgnoreCase(email.trim())
                .ifPresent(loginUserRepository::delete);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncDoctorsAndPatientsOnStartup() {
        // Keep existing doctor/patient records login-ready in the unified login table.
        adminRepository.findAll().stream()
                .filter(doctor -> doctor != null && "DOCTOR".equalsIgnoreCase(doctor.getRole()))
                .forEach(doctor -> {
                    try {
                        upsertDoctor(doctor);
                    } catch (RuntimeException ex) {
                        logger.warn("Skipped doctor login sync for email {}: {}", doctor.getEmail(), ex.getMessage());
                    }
                });

        patientRepository.findAll().forEach(patient -> {
            try {
                upsertPatient(patient);
            } catch (RuntimeException ex) {
                logger.warn("Skipped patient login sync for email {}: {}", patient.getEmail(), ex.getMessage());
            }
        });
    }
}
