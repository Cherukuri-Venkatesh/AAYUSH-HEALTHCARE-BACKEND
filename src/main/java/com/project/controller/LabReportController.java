package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.dto.LabReportDTO;
import com.project.service.LabReportService;

@RestController
@RequestMapping("/api/labreports")
@CrossOrigin(origins = "*")
public class LabReportController {

    @Autowired
    private LabReportService labReportService;

    @PostMapping("/upload")
    public LabReportDTO upload(
            @RequestParam Long appointmentId,
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam MultipartFile file) {

        return labReportService.uploadReport(
                appointmentId, doctorId, patientId, file);
    }
}