package com.project.dto;

import lombok.Data;

@Data
public class DoctorDTO {

    private Long id;

    private String name;

    private String email;

    private String specialization;

    private Integer consultingFees;

    private String degree;

    private String experience;

    private String addressLine1;

    // ❌ removed addressLine2

    private String aboutDoctor;

    private String whatsappNumber;

    // ❌ removed photoPath

    // ✅ added
    private Long hospitalId;

    // ✅ kept
    private String hospitalName;
}