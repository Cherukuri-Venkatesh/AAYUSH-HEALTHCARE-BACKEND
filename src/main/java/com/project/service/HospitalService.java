package com.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.dto.HospitalDTO;
import com.project.entity.Hospital;
import com.project.exception.ResourceNotFoundException;
import com.project.repository.HospitalRepository;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    // ===============================
    // ADD HOSPITAL
    // ===============================
    public HospitalDTO saveHospital(Hospital hospital) {

        if (hospital == null) {
            throw new RuntimeException("Hospital data is required");
        }

        String normalizedName = normalizeName(hospital.getName());
        String normalizedEmail = normalizeEmail(hospital.getEmail());
        hospital.setName(normalizedName);
        hospital.setEmail(normalizedEmail);

        if (hospitalRepository.existsByNormalizedName(normalizedName)) {
            throw new RuntimeException("Hospital already exists");
        }

        if (hospitalRepository.existsByNormalizedEmail(normalizedEmail)) {
            throw new RuntimeException("Hospital email already exists");
        }

        Hospital saved = hospitalRepository.save(hospital);

        return mapToDTO(saved);
    }

    // ===============================
    // GET ALL HOSPITALS
    // ===============================
    public List<HospitalDTO> getAllHospitals() {

        return hospitalRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // GET HOSPITAL BY ID
    // ===============================
    public HospitalDTO getHospitalById(Long id) {

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));

        return mapToDTO(hospital);
    }

    // ===============================
    // UPDATE HOSPITAL
    // ===============================
    public HospitalDTO updateHospital(Long id, Hospital hospitalDetails) {

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));

        String normalizedName = normalizeName(hospitalDetails.getName());
        String normalizedEmail = normalizeEmail(hospitalDetails.getEmail());

        if (hospitalRepository.existsByNormalizedNameAndIdNot(normalizedName, id)) {
            throw new RuntimeException("Hospital already exists");
        }

        if (hospitalRepository.existsByNormalizedEmailAndIdNot(normalizedEmail, id)) {
            throw new RuntimeException("Hospital email already exists");
        }

        hospital.setName(normalizedName);
        hospital.setAddress(hospitalDetails.getAddress());
        hospital.setCity(hospitalDetails.getCity());
        hospital.setState(hospitalDetails.getState());
        hospital.setPincode(hospitalDetails.getPincode());
        hospital.setPhone(hospitalDetails.getPhone());
        hospital.setEmail(normalizedEmail);
        hospital.setDescription(hospitalDetails.getDescription());

        Hospital updated = hospitalRepository.save(hospital);

        return mapToDTO(updated);
    }

    // ===============================
    // DELETE HOSPITAL
    // ===============================
    public void deleteHospital(Long id) {

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));

        hospitalRepository.delete(hospital);
    }

    // ===============================
    // CHECK HOSPITAL EXISTS
    // ===============================
    public boolean isHospitalExists(String name) {
        return hospitalRepository.existsByNormalizedName(name);
    }

    // ===============================
    // COUNT DOCTORS IN HOSPITAL
    // ===============================
    public long countDoctorsInHospital(Long hospitalId) {
        return hospitalRepository.countDoctorsByHospitalId(hospitalId);
    }

    // ===============================
    // HOSPITALS WITH MIN DOCTORS
    // ===============================
    public List<HospitalDTO> getHospitalsWithMinimumDoctors(int minDoctors) {

        return hospitalRepository.findHospitalsWithMinimumDoctors(minDoctors)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // ENTITY → DTO
    // ===============================
    private HospitalDTO mapToDTO(Hospital hospital) {

        return HospitalDTO.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .city(hospital.getCity())
                .state(hospital.getState())
                .pincode(hospital.getPincode())
                .phone(hospital.getPhone())
                .email(hospital.getEmail())
                .description(hospital.getDescription())
                .build();
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Hospital name is required");
        }
        return name.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Hospital email is required");
        }
        return email.trim().toLowerCase();
    }
}