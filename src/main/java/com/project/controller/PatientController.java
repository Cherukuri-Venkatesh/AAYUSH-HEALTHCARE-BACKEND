package com.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.dto.PatientDTO;
import com.project.entity.Patient;
import com.project.service.PatientService;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins="*")
public class PatientController {

    @Autowired
    private PatientService patientService;
    @PostMapping("/register")
    public PatientDTO register(@RequestBody Patient patient) {
        return patientService.registerPatient(patient);
    }

    @GetMapping("/all")
    public List<PatientDTO> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public PatientDTO getPatient(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Patient deleted successfully"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        }
    }
}