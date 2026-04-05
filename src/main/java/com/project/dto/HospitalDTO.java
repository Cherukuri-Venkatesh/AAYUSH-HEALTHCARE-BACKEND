package com.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDTO {

    private Long id;

    private String name;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private String phone;

    private String email;

    private String description;
}