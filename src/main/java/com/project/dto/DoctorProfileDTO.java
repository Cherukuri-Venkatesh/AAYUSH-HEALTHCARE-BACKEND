package com.project.dto;

import lombok.Data;

@Data
public class DoctorProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String specialization;
    private Integer consultingFees;
    private String degree;
    private String experience;
    private String addressLine1;
    private String addressLine2;
    private String aboutDoctor;
    private String whatsappNumber;
    private String photo;
    private Long hospitalId;
    private String hospitalName;
}
