package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.dto.PrescriptionDTO;
import com.project.service.PrescriptionService;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorApiController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/upload-prescription")
    public PrescriptionDTO uploadPrescription(
            @RequestParam Long appointmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam MultipartFile file) {

        return prescriptionService.uploadPrescription(
                appointmentId,
                doctorId,
                patientId,
                file
        );
    }
}
