package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.dto.PatientHistoryDTO;
import com.project.service.PatientHistoryService;

@RestController
@RequestMapping("/api/patient-history")
@CrossOrigin(origins = "*")
public class PatientHistoryController {

    @Autowired
    private PatientHistoryService patientHistoryService;

    @GetMapping("/{patientId}")
    public PatientHistoryDTO getPatientHistory(@PathVariable Long patientId) {
        return patientHistoryService.getPatientHistory(patientId);
    }
}