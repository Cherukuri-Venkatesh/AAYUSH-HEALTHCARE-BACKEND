package com.project.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.project.entity.Admindoctor;
import com.project.entity.Patient;
import com.project.repository.AdminRepository;
import com.project.repository.PatientRepository;

@Service
public class AuthContextService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PatientRepository patientRepository;

    public String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthorized");
        }

        if ("anonymousUser".equals(auth.getPrincipal().toString())) {
            throw new RuntimeException("Unauthorized");
        }

        return auth.getName();
    }

    public String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            throw new RuntimeException("Unauthorized");
        }

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.iterator().next().getAuthority();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    public boolean isDoctor() {
        return "DOCTOR".equals(getCurrentRole());
    }

    public boolean isPatient() {
        return "PATIENT".equals(getCurrentRole());
    }

    public Long getCurrentDoctorId() {
        String email = getCurrentEmail();

        Admindoctor doctor = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));

        return doctor.getId();
    }

    public Long getCurrentPatientId() {
        String email = getCurrentEmail();

        Patient patient = patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));

        return patient.getId();
    }

    public Long resolveDoctorId(Long requestedDoctorId) {
        if (isAdmin()) {
            if (requestedDoctorId == null) {
                throw new RuntimeException("Doctor ID is required");
            }
            return requestedDoctorId;
        }

        if (isDoctor()) {
            return getCurrentDoctorId();
        }

        throw new RuntimeException("Unauthorized");
    }

    public Long resolvePatientId(Long requestedPatientId) {
        if (isAdmin()) {
            if (requestedPatientId == null) {
                throw new RuntimeException("Patient ID is required");
            }
            return requestedPatientId;
        }

        if (isPatient()) {
            return getCurrentPatientId();
        }

        throw new RuntimeException("Unauthorized");
    }
}
