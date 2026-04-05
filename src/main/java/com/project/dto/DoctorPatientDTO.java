package com.project.dto;

import lombok.Data;

@Data
public class DoctorPatientDTO {
    private Long patientId;
    private String name;
    private Long doctorId;
    private String phone;
    private Integer age;
    private Double weight;
    private String issue;
    private String appointmentDate;
    private String appointmentTime;
    private Integer consultingFees;
    private String paymentType;
    private String status;
    private Long appointmentId;
    private String prescriptionPath;
    private String labReportPath;
}
