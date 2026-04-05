package com.project.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentHistoryDTO {

    private Long appointmentId;

    private Long doctorId;
    private String doctorName;

    private String issue;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private Integer consultingFees;
    private String paymentType;
    private Integer age;
    private Double weight;

    private String status;

    private String prescriptionPath;
    private String labReportPath;
}