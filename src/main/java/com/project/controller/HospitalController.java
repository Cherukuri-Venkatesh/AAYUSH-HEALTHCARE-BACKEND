package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.dto.HospitalDTO;
import com.project.entity.Hospital;
import com.project.service.HospitalService;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    // ===============================
    // ADD HOSPITAL
    // ===============================
    @PostMapping("/add")
    public HospitalDTO addHospital(@RequestBody Hospital hospital) {
        return hospitalService.saveHospital(hospital);
    }

    // ===============================
    // GET ALL HOSPITALS
    // ===============================
    @GetMapping("/all")
    public List<HospitalDTO> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    // ===============================
    // GET HOSPITAL BY ID
    // ===============================
    @GetMapping("/{id}")
    public HospitalDTO getHospital(@PathVariable Long id) {
        return hospitalService.getHospitalById(id);
    }

    // ===============================
    // UPDATE HOSPITAL
    // ===============================
    @PutMapping("/update/{id}")
    public HospitalDTO updateHospital(@PathVariable Long id,
                                      @RequestBody Hospital hospital) {
        return hospitalService.updateHospital(id, hospital);
    }

    // ===============================
    // DELETE HOSPITAL
    // ===============================
    @DeleteMapping("/delete/{id}")
    public String deleteHospital(@PathVariable Long id) {

        hospitalService.deleteHospital(id);

        return "Hospital Deleted Successfully";
    }

    // ===============================
    // COUNT DOCTORS
    // ===============================
    @GetMapping("/{id}/doctor-count")
    public long countDoctors(@PathVariable Long id) {
        return hospitalService.countDoctorsInHospital(id);
    }

    // ===============================
    // HOSPITALS WITH MIN DOCTORS
    // ===============================
    @GetMapping("/min-doctors")
    public List<HospitalDTO> getHospitalsWithMinDoctors(@RequestParam int min) {
        return hospitalService.getHospitalsWithMinimumDoctors(min);
    }
}