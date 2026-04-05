package com.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.project.controller.AuthController;
import com.project.dto.LoginRequest;
import com.project.entity.Admindoctor;
import com.project.entity.LoginUser;
import com.project.entity.LoginUserId;
import com.project.entity.Patient;
import com.project.repository.AdminRepository;
import com.project.repository.LoginUserRepository;
import com.project.repository.PatientRepository;
import com.project.service.LoginUserService;

@SpringBootTest(properties = "app.jwt.secret=test-secret-key-which-is-at-least-32-chars")
@Transactional
public class AdminLoginTableStateVerificationTest {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private LoginUserRepository loginUserRepository;

    @Autowired
    private AuthController authController;

    @Autowired
    private LoginUserService loginUserService;

    @Test
    void verifyCurrentDbStateAfterAdminMigrationToLoginTable() {
        List<Admindoctor> allDoctorRows = adminRepository.findAll();
        List<Patient> allPatientRows = patientRepository.findAll();
        List<LoginUser> allLoginRows = loginUserRepository.findAll();

        long doctorAdmins = allDoctorRows.stream()
                .filter(d -> d.getRole() != null && "ADMIN".equalsIgnoreCase(d.getRole()))
                .count();

        // Expectation after migration: no ADMIN row should remain in doctors table.
        assertEquals(0, doctorAdmins, "ADMIN row still exists in doctors table");

        List<LoginUser> adminLoginRows = allLoginRows.stream()
                .filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();

        // At least one ADMIN account should exist in login_users for admin login.
        assertFalse(adminLoginRows.isEmpty(), "No ADMIN row found in login_users table");

        // Verify admin login works via login_users table using a known temporary credential.
        String tempAdminEmail = "verify.admin." + System.currentTimeMillis() + "@example.com";
        String tempAdminPassword = "Admin@123";
        loginUserService.upsert(909090L, "Verify Admin", tempAdminEmail, tempAdminPassword, "ADMIN");

        LoginRequest request = new LoginRequest();
        request.setEmail(tempAdminEmail);
        request.setPassword(tempAdminPassword);
        request.setRole("ADMIN");

        String token = authController.login(request);
        assertNotNull(token);
        assertTrue(token.length() > 20, "Admin login token generation failed");

        // Verify every DOCTOR row has matching login_users row by (id, role).
        for (Admindoctor doctor : allDoctorRows) {
            if (doctor.getRole() == null || !"DOCTOR".equalsIgnoreCase(doctor.getRole())) {
                continue;
            }
            LoginUserId id = new LoginUserId(doctor.getId(), "DOCTOR");
            assertTrue(loginUserRepository.findById(id).isPresent(),
                    "Missing DOCTOR login row for doctor id=" + doctor.getId());
        }

        // Verify every PATIENT row has matching login_users row by (id, role).
        for (Patient patient : allPatientRows) {
            LoginUserId id = new LoginUserId(patient.getId(), "PATIENT");
            assertTrue(loginUserRepository.findById(id).isPresent(),
                    "Missing PATIENT login row for patient id=" + patient.getId());
        }

        // Optional integrity check: login_users contains no duplicate (id, role) pairs.
        Set<String> uniquePairs = allLoginRows.stream()
                .map(u -> u.getId() + "::" + u.getRole())
                .collect(Collectors.toSet());
        assertEquals(allLoginRows.size(), uniquePairs.size(), "Duplicate (id, role) entries found in login_users");

        // Optional consistency check by email for doctor/patient roles.
        Map<String, LoginUser> byEmail = allLoginRows.stream()
                .collect(Collectors.toMap(u -> u.getEmail().toLowerCase(), u -> u, (a, b) -> a));

        for (Admindoctor doctor : allDoctorRows) {
            if (doctor.getRole() == null || !"DOCTOR".equalsIgnoreCase(doctor.getRole())) {
                continue;
            }
            LoginUser loginUser = byEmail.get(doctor.getEmail().toLowerCase());
            assertNotNull(loginUser, "Missing login_users row for doctor email=" + doctor.getEmail());
            assertEquals("DOCTOR", loginUser.getRole().toUpperCase());
        }

        for (Patient patient : allPatientRows) {
            LoginUser loginUser = byEmail.get(patient.getEmail().toLowerCase());
            assertNotNull(loginUser, "Missing login_users row for patient email=" + patient.getEmail());
            assertEquals("PATIENT", loginUser.getRole().toUpperCase());
        }
    }
}
