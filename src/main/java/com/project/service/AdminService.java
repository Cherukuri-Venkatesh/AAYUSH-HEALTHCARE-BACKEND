package com.project.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.dto.DoctorDTO;
import com.project.entity.Admindoctor;
import com.project.entity.Hospital;
import com.project.repository.AdminRepository;
import com.project.repository.AppointmentRepository;
import com.project.repository.DoctorScheduleRepository;
import com.project.repository.HospitalRepository;
import com.project.repository.PatientRepository;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private LoginUserService loginUserService;

    // ✅ ADDED ONLY THIS
    @Autowired
    private EmailService emailService;

    // ======================================
    // ADD DOCTOR
    // ======================================
    public DoctorDTO saveDoctor(
            String name,
            String email,
            String password,
            String specialization,
            Integer consultingFees,
            String degree,
            String experience,
            String addressLine1,
            String aboutDoctor,
            String whatsappNumber,
            Long hospitalId
    ) {

        String normalizedEmail = email == null ? null : email.trim().toLowerCase();

        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (adminRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Doctor email already exists");
        }

        if (patientRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Doctor email already exists");
        }

        if (adminRepository.existsByWhatsappNumber(whatsappNumber)) {
            throw new RuntimeException("Doctor phone number already exists");
        }

        if (password == null || password.trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Admindoctor doctor = new Admindoctor();
        doctor.setName(name);
        doctor.setRole("DOCTOR");
        doctor.setEmail(normalizedEmail);
        doctor.setPassword(password);
        doctor.setSpecialization(specialization);
        doctor.setConsultingFees(consultingFees);
        doctor.setDegree(degree);
        doctor.setExperience(experience);
        doctor.setAddressLine1(addressLine1);
        doctor.setAboutDoctor(aboutDoctor);
        doctor.setWhatsappNumber(whatsappNumber);

        // ✅ IMPORTANT: set BOTH
        doctor.setHospital(hospital);
        doctor.setHospitalName(hospital.getName());

        Admindoctor savedDoctor = adminRepository.save(doctor);
        loginUserService.upsertDoctor(savedDoctor);

        // ✅ ONLY ADDITION: EMAIL SENDING
        emailService.sendEmail(
                savedDoctor.getEmail(),
                "Doctor Account Created - AAYUSH HEALTHCARE",
                "Dear  " + savedDoctor.getName() + ",\n\n" +
                "Your doctor account has been successfully created with AAYUSH HEALTHCARE.\n\n" +
                "Here are your login credentials:\n\n" +
                "Email: " + savedDoctor.getEmail() + "\n" +
                "Password: " + password + "\n\n" +
                "Please log in and update your profile as needed.\n\n" +
                "For security reasons, we recommend changing your password after your first login.\n\n" +
                "If you need any assistance, please contact our support team.\n\n" +
                "Warm regards,\nAAYUSH HEALTHCARE Team"
        );

        return convertToDTO(savedDoctor);
    }

    // ======================================
    // GET ALL DOCTORS
    // ======================================
    public List<DoctorDTO> getAllDoctors() {
        return getAllDoctorRecords()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ======================================
    // GET DOCTOR BY ID
    // ======================================
    public DoctorDTO getDoctorById(Long id) {

        Admindoctor doctor = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!isDoctorRecord(doctor)) {
            throw new RuntimeException("Doctor not found");
        }

        return convertToDTO(doctor);
    }

    // ======================================
    // UPDATE DOCTOR
    // ======================================
    public DoctorDTO updateDoctor(Long id, Admindoctor doctorDetails) {

        Admindoctor doctor = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!isDoctorRecord(doctor)) {
            throw new RuntimeException("Doctor not found");
        }

        String previousEmail = doctor.getEmail();
        String requestedEmail = doctorDetails.getEmail() == null ? "" : doctorDetails.getEmail().trim().toLowerCase();

        if (requestedEmail.isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        adminRepository.findByEmailIgnoreCase(requestedEmail).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Doctor email already exists");
            }
        });

        if (patientRepository.existsByEmailIgnoreCase(requestedEmail)) {
            throw new RuntimeException("Doctor email already exists");
        }

        doctor.setName(doctorDetails.getName());
        doctor.setEmail(requestedEmail);
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setConsultingFees(doctorDetails.getConsultingFees());
        doctor.setDegree(doctorDetails.getDegree());
        doctor.setExperience(doctorDetails.getExperience());
        doctor.setAddressLine1(doctorDetails.getAddressLine1());
        doctor.setAboutDoctor(doctorDetails.getAboutDoctor());
        doctor.setWhatsappNumber(doctorDetails.getWhatsappNumber());

        if (doctorDetails.getPassword() != null && !doctorDetails.getPassword().trim().isEmpty()) {
            if (doctorDetails.getPassword().trim().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            doctor.setPassword(doctorDetails.getPassword());
        }

        // ✅ keep hospital sync
        if (doctorDetails.getHospital() != null) {
            doctor.setHospital(doctorDetails.getHospital());
            doctor.setHospitalName(doctorDetails.getHospital().getName());
        }

        Admindoctor savedDoctor = adminRepository.save(doctor);

        if (previousEmail != null && !previousEmail.equalsIgnoreCase(savedDoctor.getEmail())) {
            loginUserService.deleteByEmail(previousEmail);
        }

        loginUserService.upsertDoctor(savedDoctor);
        return convertToDTO(savedDoctor);
    }

    // ======================================
    // DELETE
    // ======================================
    @Transactional
    public void deleteDoctor(Long id) {
        Admindoctor doctor = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!isDoctorRecord(doctor)) {
            throw new RuntimeException("Doctor not found");
        }

        cleanupAndDeleteDoctor(doctor);
    }

    @Transactional
    public void deleteDoctorByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Admindoctor doctor = adminRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!isDoctorRecord(doctor)) {
            throw new RuntimeException("Doctor not found");
        }

        cleanupAndDeleteDoctor(doctor);
    }

    private void cleanupAndDeleteDoctor(Admindoctor doctor) {
        Long doctorId = doctor.getId();
        String email = doctor.getEmail();

        doctorScheduleRepository.deleteByDoctor_Id(doctorId);
        appointmentRepository.deleteByDoctor_Id(doctorId);
        loginUserService.deleteByEmail(email);
        adminRepository.deleteById(doctorId);
    }

    // ======================================
    // FILTER METHODS
    // ======================================

    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return adminRepository.findBySpecialization(specialization)
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DoctorDTO getDoctorByName(String name) {
        Admindoctor doctor = adminRepository.findByName(name);
        if (!isDoctorRecord(doctor)) {
            throw new RuntimeException("Doctor not found");
        }
        return convertToDTO(doctor);
    }

    public List<DoctorDTO> getDoctorsBySpecAndFees(String specialization, Integer fees) {
        return adminRepository
                .findBySpecializationAndConsultingFeesGreaterThan(specialization, fees)
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsBySpecOrDegree(String specialization, String degree) {
        return adminRepository
                .findBySpecializationOrDegree(specialization, degree)
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsByFeesRange(Integer min, Integer max) {
        return adminRepository.findByConsultingFeesBetween(min, max)
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> searchDoctorsByName(String keyword) {
        return adminRepository.findByNameContaining(keyword)
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsByFeesGreaterThan(Integer fees) {
        return adminRepository.findByConsultingFeesGreaterThan(fees)
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> sortDoctorsByFees() {
        return adminRepository.sortByFeesAsc()
                .stream()
                .filter(this::isDoctorRecord)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long countDoctorsBySpecialization(String specialization) {
        return getDoctorsBySpecialization(specialization).size();
    }

    public boolean isEmailExists(String email) {
        return adminRepository.existsByEmail(email);
    }

    // ======================================
    // ADMIN DASHBOARD
    // ======================================
    public Map<String, Object> getAdminDashboard() {

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalPatients", patientRepository.count());
        dashboard.put("totalDoctors", getAllDoctorRecords().size());
        dashboard.put("totalAppointments", appointmentRepository.count());
        dashboard.put("completedAppointments",
                appointmentRepository.countByStatus("COMPLETED"));
        dashboard.put("cancelledAppointments",
                appointmentRepository.countByStatus("CANCELLED"));

        Integer revenue = appointmentRepository.getTotalSystemEarnings();
        if (revenue == null) revenue = 0;

        dashboard.put("totalRevenue", revenue);

        return dashboard;
    }

    // ======================================
    // DTO CONVERTER
    // ======================================
    private DoctorDTO convertToDTO(Admindoctor doctor) {
        DoctorDTO dto = modelMapper.map(doctor, DoctorDTO.class);

        if (doctor.getHospital() != null) {
            dto.setHospitalId(doctor.getHospital().getId());
            dto.setHospitalName(doctor.getHospital().getName());
        } else {
            dto.setHospitalId(null);
            dto.setHospitalName(doctor.getHospitalName());
        }

        return dto;
    }

    private boolean isDoctorRecord(Admindoctor doctor) {
        return doctor != null && "DOCTOR".equalsIgnoreCase(doctor.getRole());
    }

    private List<Admindoctor> getAllDoctorRecords() {
        return adminRepository.findAll()
                .stream()
                .filter(this::isDoctorRecord)
                .collect(Collectors.toList());
    }

}