package com.project.dto;

import lombok.Data;

@Data
public class PatientDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private String gender;
    private String addressLine1;
    private String addressLine2;
}