package com.project.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class AppointmentDTO {

    private Long id;

    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String hospitalName;

    private Integer age;
    private Double weight;
    private String issue;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String status;
    private Integer consultingFees;
    private String paymentType;

    private String prescriptionPath;
    private String labReportPath;
}