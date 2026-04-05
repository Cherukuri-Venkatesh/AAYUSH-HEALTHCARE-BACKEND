package com.project.dto;

import lombok.Data;

@Data
public class DoctorProfileUpdateRequest {
    private String name;
    private String specialization;
    private Integer consultingFees;
    private String degree;
    private String experience;
    private String addressLine1;
    private String aboutDoctor;
    private String whatsappNumber;
    private String password;
}
