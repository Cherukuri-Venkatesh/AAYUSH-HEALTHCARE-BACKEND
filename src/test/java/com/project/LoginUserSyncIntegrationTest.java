package com.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.project.controller.AuthController;
import com.project.dto.DoctorDTO;
import com.project.dto.LoginRequest;
import com.project.dto.PatientDTO;
import com.project.dto.PatientProfileUpdateRequest;
import com.project.entity.Admindoctor;
import com.project.entity.Hospital;
import com.project.entity.LoginUser;
import com.project.entity.LoginUserId;
import com.project.entity.Patient;
import com.project.repository.HospitalRepository;
import com.project.repository.LoginUserRepository;
import com.project.service.AdminService;
import com.project.service.LoginUserService;
import com.project.service.PatientService;

@SpringBootTest(properties = "app.jwt.secret=test-secret-key-which-is-at-least-32-chars")
@Transactional
public class LoginUserSyncIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private LoginUserService loginUserService;

    @Autowired
    private LoginUserRepository loginUserRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AuthController authController;

        @Autowired
        private PasswordEncoder passwordEncoder;

    @Test
    void verifyDoctorPatientInsertUpdateDeleteAndLoginFlow() {
        long suffix = System.currentTimeMillis();

        Hospital hospital = new Hospital();
        hospital.setName("Test Hospital " + suffix);
        hospital.setCity("City");
        hospital.setState("State");
        hospital.setAddress("Address");
        hospital.setPhone("9000012345");
        hospital.setEmail("hospital." + suffix + "@example.com");
        hospital.setPincode("500001");
        hospital.setDescription("Test");
        Hospital savedHospital = hospitalRepository.save(hospital);

        // 1) DOCTOR INSERT -> login_users sync with same id
        String doctorEmail = "doctor." + suffix + "@example.com";
        String doctorPhone = "9" + String.valueOf(100000000L + (suffix % 899999999L));
        DoctorDTO createdDoctor = adminService.saveDoctor(
                "Doctor " + suffix,
                doctorEmail,
                "Doctor@123",
                "Cardiology",
                1000,
                "MBBS",
                "2 years",
                "Address 1",
                "About",
                doctorPhone,
                savedHospital.getId());

        LoginUser createdDoctorLogin = loginUserRepository.findById(
                new LoginUserId(createdDoctor.getId(), "DOCTOR"))
                .orElseThrow(() -> new RuntimeException("Doctor login row missing"));

        assertEquals(createdDoctor.getId(), createdDoctorLogin.getId());
        assertEquals(doctorEmail, createdDoctorLogin.getEmail());
        assertEquals("DOCTOR", createdDoctorLogin.getRole());

        // 2) DOCTOR UPDATE PASSWORD -> both doctors + login_users updated
        Admindoctor updateDoctor = new Admindoctor();
        updateDoctor.setName("Doctor Updated " + suffix);
        updateDoctor.setEmail(doctorEmail);
        updateDoctor.setPassword("Doctor@456");
        updateDoctor.setSpecialization("Cardiology");
        updateDoctor.setConsultingFees(1200);
        updateDoctor.setDegree("MBBS");
        updateDoctor.setExperience("3 years");
        updateDoctor.setAddressLine1("Address Updated");
        updateDoctor.setAboutDoctor("About Updated");
        updateDoctor.setWhatsappNumber(doctorPhone);
        updateDoctor.setHospital(savedHospital);

        adminService.updateDoctor(createdDoctor.getId(), updateDoctor);

        LoginUser updatedDoctorLogin = loginUserRepository.findById(
                new LoginUserId(createdDoctor.getId(), "DOCTOR"))
                .orElseThrow(() -> new RuntimeException("Updated doctor login row missing"));

        assertTrue(passwordEncoder.matches("Doctor@456", updatedDoctorLogin.getPassword()));

        // 3) PATIENT INSERT -> login_users sync with same id
        Patient patient = new Patient();
        patient.setName("Patient " + suffix);
        patient.setAge(30);
        patient.setEmail("patient." + suffix + "@example.com");
        patient.setPhone("8" + String.valueOf(100000000L + (suffix % 899999999L)));
        patient.setPassword("Patient@123");
        patient.setAddressLine1("P Address 1");
        patient.setAddressLine2("P Address 2");
        patient.setGender("MALE");

        PatientDTO createdPatient = patientService.registerPatient(patient);

        LoginUser createdPatientLogin = loginUserRepository.findById(
                new LoginUserId(createdPatient.getId(), "PATIENT"))
                .orElseThrow(() -> new RuntimeException("Patient login row missing"));

        assertEquals(createdPatient.getId(), createdPatientLogin.getId());
        assertEquals(createdPatient.getEmail(), createdPatientLogin.getEmail());
        assertEquals("PATIENT", createdPatientLogin.getRole());

        // 4) PATIENT PROFILE UPDATE + PASSWORD UPDATE -> login_users updated
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(createdPatient.getEmail(), null, List.of(() -> "PATIENT")));

        PatientProfileUpdateRequest profileRequest = new PatientProfileUpdateRequest();
        profileRequest.setName("Patient Updated " + suffix);
        profileRequest.setAge(31);
        profileRequest.setGender("MALE");
        profileRequest.setAddressLine1("New Addr 1");
        profileRequest.setAddressLine2("New Addr 2");
        patientService.updateCurrentPatientProfile(profileRequest);
        patientService.updateCurrentPatientPassword("Patient@456");

        LoginUser updatedPatientLogin = loginUserRepository.findById(
                new LoginUserId(createdPatient.getId(), "PATIENT"))
                .orElseThrow(() -> new RuntimeException("Updated patient login row missing"));

        assertEquals("Patient Updated " + suffix, updatedPatientLogin.getName());
        assertTrue(passwordEncoder.matches("Patient@456", updatedPatientLogin.getPassword()));

        // 5) LOGIN FLOW via login_users table
        loginUserService.upsert(777777L, "Admin Test", "admin." + suffix + "@example.com", "Admin@123", "ADMIN");

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin." + suffix + "@example.com");
        adminLogin.setPassword("Admin@123");
        adminLogin.setRole("ADMIN");

        LoginRequest doctorLogin = new LoginRequest();
        doctorLogin.setEmail(doctorEmail);
        doctorLogin.setPassword("Doctor@456");
        doctorLogin.setRole("DOCTOR");

        LoginRequest patientLogin = new LoginRequest();
        patientLogin.setEmail(createdPatient.getEmail());
        patientLogin.setPassword("Patient@456");
        patientLogin.setRole("PATIENT");

        assertDoesNotThrow(() -> {
            String token = authController.login(adminLogin);
            assertNotNull(token);
            assertTrue(token.length() > 20);
        });

        assertDoesNotThrow(() -> {
            String token = authController.login(doctorLogin);
            assertNotNull(token);
            assertTrue(token.length() > 20);
        });

        assertDoesNotThrow(() -> {
            String token = authController.login(patientLogin);
            assertNotNull(token);
            assertTrue(token.length() > 20);
        });

        // 6) SAME id for different roles should coexist
        loginUserService.upsert(555555L, "SameId Doctor", "sameid.doctor." + suffix + "@example.com", "x", "DOCTOR");
        loginUserService.upsert(555555L, "SameId Patient", "sameid.patient." + suffix + "@example.com", "x", "PATIENT");

        assertTrue(loginUserRepository.findById(new LoginUserId(555555L, "DOCTOR")).isPresent());
        assertTrue(loginUserRepository.findById(new LoginUserId(555555L, "PATIENT")).isPresent());

        // 7) DELETE sync checks
        adminService.deleteDoctor(createdDoctor.getId());
        assertFalse(loginUserRepository.findById(new LoginUserId(createdDoctor.getId(), "DOCTOR")).isPresent());

        patientService.deletePatient(createdPatient.getId());
        assertFalse(loginUserRepository.findById(new LoginUserId(createdPatient.getId(), "PATIENT")).isPresent());
    }
}
