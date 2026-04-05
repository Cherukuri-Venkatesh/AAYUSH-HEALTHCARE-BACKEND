package com.project.dto;

import lombok.Data;

@Data
public class PatientProfileUpdateRequest {
    private String name;
    private Integer age;
    private String gender;
    private String addressLine1;
    private String addressLine2;
}
