package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.dto.AppointmentDTO;
import com.project.dto.DoctorPatientDTO;
import com.project.dto.DoctorProfileDTO;
import com.project.dto.DoctorProfileUpdateRequest;
import com.project.entity.Admindoctor;
import com.project.repository.AdminRepository;
import com.project.security.AuthContextService;
import com.project.service.AppointmentService;
import com.project.service.LoginUserService;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorSelfController {

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private LoginUserService loginUserService;

    @GetMapping("/me")
    public DoctorProfileDTO getMyProfile() {
        ensureDoctorAccess();
        Long doctorId = authContextService.getCurrentDoctorId();

        Admindoctor doctor = adminRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setConsultingFees(doctor.getConsultingFees());
        dto.setDegree(doctor.getDegree());
        dto.setExperience(doctor.getExperience());
        dto.setAddressLine1(doctor.getAddressLine1());
        dto.setAddressLine2("");
        dto.setAboutDoctor(doctor.getAboutDoctor());
        dto.setWhatsappNumber(doctor.getWhatsappNumber());
        dto.setPhoto(null);
        dto.setHospitalId(doctor.getHospital() == null ? null : doctor.getHospital().getId());
        dto.setHospitalName(doctor.getHospitalName());

        return dto;
    }

    @PutMapping("/me")
    public DoctorProfileDTO updateMyProfile(@RequestBody DoctorProfileUpdateRequest request) {
        ensureDoctorAccess();
        Long doctorId = authContextService.getCurrentDoctorId();

        Admindoctor doctor = adminRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        doctor.setName(request.getName().trim());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setConsultingFees(request.getConsultingFees());
        doctor.setDegree(request.getDegree());
        doctor.setExperience(request.getExperience());
        doctor.setAddressLine1(request.getAddressLine1());
        doctor.setAboutDoctor(request.getAboutDoctor());
        doctor.setWhatsappNumber(request.getWhatsappNumber());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            doctor.setPassword(request.getPassword().trim());
        }

        Admindoctor saved = adminRepository.save(doctor);
        loginUserService.upsertDoctor(saved);

        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setEmail(saved.getEmail());
        dto.setSpecialization(saved.getSpecialization());
        dto.setConsultingFees(saved.getConsultingFees());
        dto.setDegree(saved.getDegree());
        dto.setExperience(saved.getExperience());
        dto.setAddressLine1(saved.getAddressLine1());
        dto.setAddressLine2("");
        dto.setAboutDoctor(saved.getAboutDoctor());
        dto.setWhatsappNumber(saved.getWhatsappNumber());
        dto.setPhoto(null);
        dto.setHospitalId(saved.getHospital() == null ? null : saved.getHospital().getId());
        dto.setHospitalName(saved.getHospitalName());

        return dto;
    }

    @GetMapping("/appointments")
    public List<AppointmentDTO> getMyAppointments() {
        return appointmentService.getCurrentDoctorAppointments();
    }

    @GetMapping("/patients")
    public List<DoctorPatientDTO> getMyPatients() {
        return appointmentService.getCurrentDoctorPatients();
    }

    private void ensureDoctorAccess() {
        if (!authContextService.isDoctor()) {
            throw new RuntimeException("Unauthorized");
        }
    }
}
